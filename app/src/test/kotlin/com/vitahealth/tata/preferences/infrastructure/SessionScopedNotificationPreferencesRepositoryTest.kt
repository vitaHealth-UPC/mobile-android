package com.vitahealth.tata.preferences.infrastructure

import com.vitahealth.tata.preferences.infrastructure.security.SessionScopedNotificationPreferencesRepository
import com.vitahealth.tata.preferences.FakeNotificationPreferencesRepository
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionScopedNotificationPreferencesRepositoryTest {
    private var current: StoredSession? = null
    private val sessions =
        object : SessionStore {
            override fun session() = current

            override fun accessToken() = current?.accessToken

            override fun save(accessToken: String, expiresAt: String) {
                current = StoredSession(accessToken, Instant.parse(expiresAt), null)
            }

            override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
                current = StoredSession(accessToken, Instant.parse(expiresAt), owner)
            }

            override fun clear() {
                current = null
            }
        }

    private fun login(id: String = "account", role: String = "CAREGIVER") =
        sessions.save("token-$id", "2099-01-01T00:00:00Z", SessionOwner(id, role))

    private val policy = NotificationPolicy(5, 10, 20, 30, null, null)

    @Test
    fun capturesCredentialsForReadsSettingsAndPolicy() = runBlocking {
        login()
        val remote = FakeNotificationPreferencesRepository()
        var calls = 0
        val repository =
            SessionScopedNotificationPreferencesRepository(sessions) { captured ->
                calls++
                assertEquals("token-account", captured.accessToken)
                assertEquals("account", captured.owner?.subjectId)
                remote
            }
        assertTrue(repository.get("account") is AppResult.Success)
        assertTrue(repository.update("account", NotificationPreferences()) is AppResult.Success)
        assertTrue(repository.updatePolicy("account", policy, emptyList()) is AppResult.Success)
        assertEquals(3, calls)
    }

    @Test
    fun noSessionUnboundSetupAndOtherOwnerNeverCallTheRemote() = runBlocking {
        val repository =
            SessionScopedNotificationPreferencesRepository(sessions) {
                error("No remote access expected")
            }
        assertEquals("SESSION_REQUIRED", (repository.get("account") as AppResult.Failure).code)
        sessions.save("legacy", "2099-01-01T00:00:00Z")
        assertEquals("SESSION_REQUIRED", (repository.get("account") as AppResult.Failure).code)
        login(role = "LINK_SETUP")
        assertEquals("SESSION_REQUIRED", (repository.get("account") as AppResult.Failure).code)
        login()
        assertEquals(
            "PREFERENCES_OWNER_MISMATCH",
            (repository.update("other", NotificationPreferences()) as AppResult.Failure).code,
        )
    }

    @Test
    fun everyOperationRejectsAResultAfterTheSessionChanges() = runBlocking {
        for (operation in 0..2) {
            login()
            val repository =
                SessionScopedNotificationPreferencesRepository(sessions) { captured ->
                    assertEquals("token-account", captured.accessToken)
                    login("other")
                    FakeNotificationPreferencesRepository()
                }
            val result =
                when (operation) {
                    0 -> repository.get("account")
                    1 -> repository.update("account", NotificationPreferences())
                    else -> repository.updatePolicy("account", policy, emptyList())
                }
            assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
        }
    }
}
