package com.vitahealth.tata.identity.infrastructure.security

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SessionScopedSubscriptionRepositoryTest {
    private val now = Instant.parse("2026-10-24T12:00:00Z")

    private fun session(
        id: String = "account-1",
        role: String = "CAREGIVER",
        token: String = "first",
    ) = StoredSession(token, now.plusSeconds(300), SessionOwner(id, role))

    @Test
    fun invalidSessionsNeverConstructRemoteForAnyOperation() = runTest {
        listOf(
                null,
                session(role = "OLDER_ADULT"),
                session("other"),
                StoredSession("expired", now, SessionOwner("account-1", "CAREGIVER")),
            )
            .forEach { value ->
                val repository =
                    SessionScopedSubscriptionRepository(
                        "account-1",
                        Store(value),
                        { error("remote must not be created") },
                        { now },
                    )
                assertTrue(repository.listPlans() is AppResult.Failure)
                assertTrue(repository.currentSubscription("account-1") is AppResult.Failure)
                assertTrue(
                    repository.changeSubscription("account-1", "ESSENTIAL") is AppResult.Failure
                )
            }
    }

    @Test
    fun requestForAnotherAccountNeverConstructsRemote() = runTest {
        val repository =
            SessionScopedSubscriptionRepository(
                "account-1",
                Store(session()),
                { error("remote must not be created") },
                { now },
            )
        assertEquals(
            "ACCOUNT_ACCESS_DENIED",
            (repository.currentSubscription("other") as AppResult.Failure).code,
        )
        assertEquals(
            "ACCOUNT_ACCESS_DENIED",
            (repository.changeSubscription("other", "ESSENTIAL") as AppResult.Failure).code,
        )
    }

    @Test
    fun changedSessionDiscardsResultsWhileUsingCapturedCredentials() = runTest {
        for (operation in 0..2) {
            val store = Store(session())
            var token: String? = null
            val repository =
                SessionScopedSubscriptionRepository(
                    "account-1",
                    store,
                    { captured ->
                        token = captured.accessToken
                        store.value = session("new-account", token = "new-token")
                        FakeSubscriptionRepository()
                    },
                    { now },
                )
            val result =
                when (operation) {
                    0 -> repository.listPlans()
                    1 -> repository.currentSubscription("account-1")
                    else -> repository.changeSubscription("account-1", "ESSENTIAL")
                }
            assertEquals("first", token)
            assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
            assertEquals("new-token", store.value?.accessToken)
        }
    }

    private class Store(var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun clear() {
            value = null
        }

        override fun save(accessToken: String, expiresAt: String) {
            value = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            value = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }
    }
}
