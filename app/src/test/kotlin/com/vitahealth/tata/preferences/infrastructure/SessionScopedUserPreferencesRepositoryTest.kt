package com.vitahealth.tata.preferences.infrastructure

import com.vitahealth.tata.preferences.infrastructure.security.SessionScopedUserPreferencesRepository
import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRemote
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionScopedUserPreferencesRepositoryTest {
    private class Sessions : SessionStore, SessionObserver {
        val state = MutableStateFlow<StoredSession?>(null)

        override fun observeSessions() = state

        override fun session() = state.value

        override fun accessToken() = state.value?.accessToken

        override fun save(accessToken: String, expiresAt: String) {
            state.value = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            state.value = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }

        override fun clear() {
            state.value = null
        }
    }

    private val sessions = Sessions()
    private val a = SessionOwner("a", "CAREGIVER")
    private val b = SessionOwner("b", "CAREGIVER")
    private val locals = mutableMapOf<SessionOwner, FakeAccessibilityLocalStore>()
    private val remotes = mutableMapOf<SessionOwner, FakeUserPreferencesRemote>()

    private fun login(owner: SessionOwner) =
        sessions.save("token-${owner.subjectId}", "2099-01-01T00:00:00Z", owner)

    private fun repository() =
        SessionScopedUserPreferencesRepository(
            sessions,
            sessions,
            { captured ->
                assertEquals("token-${captured.owner!!.subjectId}", captured.accessToken)
                remotes.getOrPut(captured.owner) { FakeUserPreferencesRemote() }
            },
            { owner -> locals.getOrPut(owner) { FakeAccessibilityLocalStore() } },
        )

    @Test
    fun queuedEditRevalidatesTheSessionAfterWaitingForSync() = runBlocking {
        login(a)
        val gate = CompletableDeferred<Unit>()
        val delegate = FakeUserPreferencesRemote()
        val repository =
            SessionScopedUserPreferencesRepository(
                sessions,
                sessions,
                {
                    object : UserPreferencesRemote by delegate {
                        override suspend fun get(userId: String): AppResult<UserPreferences> {
                            gate.await()
                            return delegate.get(userId)
                        }
                    }
                },
                { owner -> locals.getOrPut(owner) { FakeAccessibilityLocalStore() } },
            )
        val sync = launch(start = CoroutineStart.UNDISPATCHED) { repository.sync("a") }
        var result: AppResult<*>? = null
        val edit =
            launch(start = CoroutineStart.UNDISPATCHED) {
                result = repository.updateHighContrast("a", true)
            }
        assertTrue(delegate.contrastCalls.isEmpty())
        login(b)
        gate.complete(Unit)
        sync.join()
        edit.join()
        assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
        assertTrue(delegate.contrastCalls.isEmpty())
        assertFalse(locals.getValue(a).current().highContrast)
    }

    @Test
    fun pendingChangesFromOneAccountAreNotPushedByAnother() = runBlocking {
        val repository = repository()
        login(a)
        remotes[a] = FakeUserPreferencesRemote().apply { failWith = NetworkDown }
        repository.updateTextSize("a", TextSizeLevel.LARGE)
        assertTrue(locals.getValue(a).isPendingSync())
        login(b)
        repository.sync("b")
        assertTrue(remotes.getValue(b).textSizeCalls.isEmpty())
        assertEquals(TextSizeLevel.MEDIUM, locals.getValue(b).current().textSize)
        assertTrue(locals.getValue(a).isPendingSync())
        login(a)
        remotes.getValue(a).failWith = null
        repository.sync("a")
        assertFalse(locals.getValue(a).isPendingSync())
        assertEquals(TextSizeLevel.LARGE, locals.getValue(a).current().textSize)
    }

    @Test
    fun observationUsesTheCurrentAccountAndDefaultsAfterLogout() = runBlocking {
        val repository = repository()
        login(a)
        repository.updateHighContrast("a", true)
        assertTrue(repository.observeAccessibility().first().highContrast)
        login(b)
        assertFalse(repository.observeAccessibility().first().highContrast)
        sessions.clear()
        assertFalse(repository.observeAccessibility().first().highContrast)
    }

    @Test
    fun missingAndMismatchedOwnersNeverAccessTheRemote() = runBlocking {
        val repository = repository()
        assertEquals("SESSION_REQUIRED", (repository.sync("a") as AppResult.Failure).code)
        login(a)
        assertEquals("PREFERENCES_OWNER_MISMATCH", (repository.sync("b") as AppResult.Failure).code)
        assertTrue(remotes.isEmpty())
    }

    @Test
    fun oldResponseCannotUpdateTheNewAccountsPartitionOrReportSuccess() = runBlocking {
        login(a)
        val delegate = FakeUserPreferencesRemote()
        val repository =
            SessionScopedUserPreferencesRepository(
                sessions,
                sessions,
                {
                    object : UserPreferencesRemote by delegate {
                        override suspend fun updateHighContrast(
                            userId: String,
                            enabled: Boolean,
                        ): AppResult<UserPreferences> {
                            login(b)
                            return delegate.updateHighContrast(userId, enabled)
                        }
                    }
                },
                { owner -> locals.getOrPut(owner) { FakeAccessibilityLocalStore() } },
            )
        assertEquals(
            "SESSION_CHANGED",
            (repository.updateHighContrast("a", true) as AppResult.Failure).code,
        )
        assertFalse(repository.observeAccessibility().first().highContrast)
        assertTrue(locals.getValue(a).current().highContrast)
    }
}
