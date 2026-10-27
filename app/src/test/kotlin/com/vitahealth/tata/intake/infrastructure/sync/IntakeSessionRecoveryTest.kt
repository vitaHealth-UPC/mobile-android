package com.vitahealth.tata.intake.infrastructure.sync

import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.intake.application.ports.SyncScheduler
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Proves recovery is tied to usable session changes instead of opening an intake screen. */
@OptIn(ExperimentalCoroutinesApi::class)
class IntakeSessionRecoveryTest {
    private val now = Instant.parse("2026-10-22T12:00:00Z")
    private val owner = SessionOwner("account-a", "CAREGIVER")
    private val observer = Observer()
    private val scheduler = Scheduler()
    private val recovery = IntakeSessionRecovery(observer, scheduler) { now }

    private fun session(
        token: String = "token-a",
        owner: SessionOwner? = this.owner,
        expiresAt: Instant = now.plusSeconds(3600),
    ) = StoredSession(token, expiresAt, owner)

    @Test
    fun unboundSetupExpiredAndAbsentSessionsDoNotStartReplay() = runTest {
        recovery.start(backgroundScope)
        runCurrent()
        for (value in
            listOf(
                session(owner = null),
                session(owner = SessionOwner("adult", "LINK_SETUP")),
                session(expiresAt = now.minusSeconds(1)),
                null,
            )) {
            observer.state.value = value
            runCurrent()
        }
        assertEquals(0, scheduler.calls)
    }

    @Test
    fun startupLoginRenewalAndAccountChangeResumeWithoutOpeningAnyScreen() = runTest {
        observer.state.value = session()
        recovery.start(backgroundScope)
        runCurrent()
        assertEquals(1, scheduler.calls)
        observer.state.value = session()
        runCurrent()
        assertEquals(1, scheduler.calls)
        observer.state.value = session(token = "renewed-token")
        runCurrent()
        assertEquals(2, scheduler.calls)
        observer.state.value =
            session(token = "account-b-token", owner = SessionOwner("account-b", "CAREGIVER"))
        runCurrent()
        assertEquals(3, scheduler.calls)
    }

    @Test
    fun bindingALegacySessionStartsItsFirstRecovery() = runTest {
        observer.state.value = session(owner = null)
        recovery.start(backgroundScope)
        runCurrent()
        assertEquals(0, scheduler.calls)
        observer.state.value = session()
        runCurrent()
        assertEquals(1, scheduler.calls)
    }

    @Test
    fun logoutClearsThePreviousRecoveryIdentity() = runTest {
        observer.state.value = session()
        recovery.start(backgroundScope)
        runCurrent()
        observer.state.value = null
        runCurrent()
        observer.state.value = session()
        runCurrent()
        assertEquals(2, scheduler.calls)
    }

    @Test
    fun startIsIdempotentAndCancelledCollectorCanBeRestarted() = runTest {
        observer.state.value = session()
        val first = recovery.start(backgroundScope)
        assertSame(first, recovery.start(backgroundScope))
        runCurrent()
        assertEquals(1, scheduler.calls)
        first.cancel()
        runCurrent()
        observer.state.value = session(token = "new")
        runCurrent()
        assertEquals(1, scheduler.calls)
        recovery.start(backgroundScope)
        runCurrent()
        assertEquals(2, scheduler.calls)
    }

    private class Observer : SessionObserver {
        val state = MutableStateFlow<StoredSession?>(null)

        override fun observeSessions() = state
    }

    private class Scheduler : SyncScheduler {
        var calls = 0

        override fun schedule() {
            calls++
        }
    }
}
