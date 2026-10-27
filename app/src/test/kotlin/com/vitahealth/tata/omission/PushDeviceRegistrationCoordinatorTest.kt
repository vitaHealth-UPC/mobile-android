package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.ports.DevicePushTokenSource
import com.vitahealth.tata.omission.application.ports.DeviceTokenRegistry
import com.vitahealth.tata.omission.application.services.PushDeviceRegistrationCoordinator
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exercises token/session ordering with a process scope, without Android or live Firebase. */
@OptIn(ExperimentalCoroutinesApi::class)
class PushDeviceRegistrationCoordinatorTest {
    private val sessions = MemorySessions()
    private val registry = RecordingRegistry()
    private val first = StoredSession("first", Instant.MAX, SessionOwner("a", "CAREGIVER"))
    private val second = StoredSession("second", Instant.MAX, SessionOwner("b", "CAREGIVER"))

    @Test
    fun aTemporaryRegistrationFailureRecoversWithoutAnotherLogin() = runTest {
        sessions.current.value = first
        registry.failuresRemaining = 1
        val coordinator = coordinator { AppResult.Success("sdk-token") }
        coordinator.start(backgroundScope)
        runCurrent()
        assertEquals(1, registry.tokens.size)
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(listOf("sdk-token", "sdk-token"), registry.tokens)
        advanceTimeBy(100_000)
        runCurrent()
        assertEquals(2, registry.tokens.size)
    }

    @Test
    fun authorizationFailuresNeverRetryAndTransientRetriesAreBounded() = runTest {
        sessions.current.value = first
        registry.failureCode = "SESSION_REQUIRED"
        registry.failuresRemaining = 10
        val coordinator = coordinator { AppResult.Success("sdk-token") }
        coordinator.start(backgroundScope)
        runCurrent()
        advanceTimeBy(100_000)
        runCurrent()
        assertEquals(1, registry.tokens.size)
        registry.failureCode = "NETWORK_UNAVAILABLE"
        coordinator.onNewToken("rotated")
        runCurrent()
        for (delay in listOf(5_001L, 15_001L, 60_001L, 100_000L)) {
            advanceTimeBy(delay)
            runCurrent()
        }
        assertEquals(5, registry.tokens.size)
    }

    @Test
    fun logoutCancelsTheEffectOfPendingRegistrationBackoff() = runTest {
        sessions.current.value = first
        registry.failuresRemaining = 10
        val coordinator = coordinator { AppResult.Success("sdk-token") }
        coordinator.start(backgroundScope)
        runCurrent()
        coordinator.beforeSignOut()
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(1, registry.tokens.size)
        assertEquals(listOf(first), registry.removedSessions)
    }

    @Test
    fun temporarySdkFailureIsRetriedWithoutInventingAToken() = runTest {
        sessions.current.value = first
        var reads = 0
        val coordinator = coordinator {
            reads++
            if (reads == 1) AppResult.Failure("Unavailable", code = "PUSH_TOKEN_UNAVAILABLE")
            else AppResult.Success("sdk-token")
        }
        coordinator.start(backgroundScope)
        runCurrent()
        assertTrue(registry.tokens.isEmpty())
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(listOf("sdk-token"), registry.tokens)
    }

    @Test
    fun loginRecoversTheTokenAlreadyStoredByFirebase() = runTest {
        var reads = 0
        val coordinator = coordinator {
            reads++
            AppResult.Success("sdk-token")
        }
        coordinator.start(backgroundScope)
        runCurrent()
        assertEquals(0, reads)
        sessions.current.value = first
        runCurrent()
        assertEquals(listOf("sdk-token"), registry.tokens)
        assertEquals(listOf(first), registry.registeredSessions)
    }

    @Test
    fun tokenRotationBeforeAndAfterLoginUsesTheLatestAddress() = runTest {
        val coordinator = coordinator { error("Cached callback token must be used") }
        coordinator.start(backgroundScope)
        coordinator.onNewToken("before-login")
        runCurrent()
        assertTrue(registry.tokens.isEmpty())
        sessions.current.value = first
        runCurrent()
        coordinator.onNewToken("rotated")
        runCurrent()
        assertEquals(listOf("before-login", "rotated"), registry.tokens)
    }

    @Test
    fun cancelledSdkReadCannotRegisterUnderTheReplacementAccount() = runTest {
        lateinit var oldRead: Continuation<AppResult<String?>>
        var reads = 0
        val coordinator = coordinator {
            reads++
            if (reads == 1) suspendCoroutine { oldRead = it } else AppResult.Success("new-token")
        }
        sessions.current.value = first
        coordinator.start(backgroundScope)
        runCurrent()
        sessions.current.value = second
        runCurrent()
        oldRead.resume(AppResult.Success("old-token"))
        runCurrent()
        assertEquals(listOf("new-token"), registry.tokens)
        assertEquals(listOf(second), registry.registeredSessions)
    }

    @Test
    fun logoutRemovesRegistrationAndPreventsReregistrationOfTheEndingSession() = runTest {
        sessions.current.value = first
        val coordinator = coordinator { AppResult.Success("sdk-token") }
        coordinator.start(backgroundScope)
        runCurrent()
        coordinator.beforeSignOut()
        coordinator.onNewToken("during-logout")
        runCurrent()
        assertEquals(listOf(first), registry.removedSessions)
        assertEquals(1, registry.tokens.size)
        sessions.current.value = second
        runCurrent()
        assertEquals(listOf(first, second), registry.registeredSessions)
    }

    @Test
    fun unconfiguredFirebaseNeverRegistersOrDelaysLogout() = runTest {
        sessions.current.value = first
        val coordinator = coordinator { AppResult.Success(null) }
        val job = coordinator.start(backgroundScope)
        assertTrue(job === coordinator.start(backgroundScope))
        runCurrent()
        coordinator.beforeSignOut()
        assertTrue(registry.tokens.isEmpty())
        assertTrue(registry.removedSessions.isEmpty())
    }

    @Test
    fun unavailableUnregistrationHasABoundedWait() = runTest {
        sessions.current.value = first
        registry.holdRemoval = true
        val coordinator = coordinator { AppResult.Success("sdk-token") }
        coordinator.start(backgroundScope)
        runCurrent()
        val closing = launch { coordinator.beforeSignOut() }
        runCurrent()
        assertTrue(closing.isActive)
        advanceTimeBy(5_001)
        runCurrent()
        assertTrue(closing.isCompleted)
    }

    private fun coordinator(read: suspend () -> AppResult<String?>) =
        PushDeviceRegistrationCoordinator(sessions, sessions, DevicePushTokenSource(read), registry)

    private class RecordingRegistry : DeviceTokenRegistry {
        val tokens = mutableListOf<String>()
        val registeredSessions = mutableListOf<StoredSession>()
        val removedSessions = mutableListOf<StoredSession>()
        var holdRemoval = false
        var failuresRemaining = 0
        var failureCode = "NETWORK_UNAVAILABLE"

        override suspend fun register(
            token: String,
            expectedSession: StoredSession?,
        ): AppResult<Unit> {
            tokens += token
            registeredSessions += requireNotNull(expectedSession)
            if (failuresRemaining-- > 0) return AppResult.Failure("Unavailable", code = failureCode)
            return AppResult.Success(Unit)
        }

        override suspend fun unregister(expectedSession: StoredSession?): AppResult<Unit> {
            removedSessions += requireNotNull(expectedSession)
            if (holdRemoval) awaitCancellation()
            return AppResult.Success(Unit)
        }
    }

    private class MemorySessions : SessionStore, SessionObserver {
        val current = MutableStateFlow<StoredSession?>(null)

        override fun observeSessions() = current

        override fun session() = current.value

        override fun accessToken() = current.value?.accessToken

        override fun save(accessToken: String, expiresAt: String) {
            current.value = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            current.value = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }

        override fun clear() {
            current.value = null
        }
    }
}
