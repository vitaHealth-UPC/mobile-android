package com.vitahealth.tata.analytics.infrastructure

import com.vitahealth.tata.analytics.infrastructure.remote.AnalyticsApiService
import com.vitahealth.tata.analytics.infrastructure.security.SessionScopedAnalyticsRepositories
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

/** Exercises both analytics operations across missing, replaced and adult-scoped credentials. */
class SessionScopedAnalyticsRepositoriesTest {
    private val now = Instant.parse("2026-10-23T12:00:00Z")
    private val owner = SessionOwner("caregiver", "CAREGIVER")

    private fun session(
        subject: SessionOwner? = owner,
        token: String = "first",
        expiry: Instant = now.plusSeconds(60),
    ) = StoredSession(token, expiry, subject)

    @Test
    fun unusableSessionsNeverCreateAnApi() = runBlocking {
        for (value in
            listOf(
                null,
                session(subject = null),
                session(expiry = now),
                session(SessionOwner("adult", "LINK_SETUP")),
            )) {
            val store = Store(value)
            val repository =
                SessionScopedAnalyticsRepositories(store, owner, { error("API must not open") }) {
                    now
                }
            assertCode("SESSION_REQUIRED", repository.getSummary("adult", 7))
            assertCode("SESSION_REQUIRED", repository.getTreatmentSummary("adult", "treatment", 7))
            assertCode("SESSION_REQUIRED", repository.getRecommendations("adult", 7))
        }
    }

    @Test
    fun oldScreenAndAnotherAdultsDataAreRejectedBeforeApiCreation() = runBlocking {
        val store = Store(session(SessionOwner("other", "CAREGIVER")))
        val oldScreen =
            SessionScopedAnalyticsRepositories(store, owner, { error("API must not open") }) { now }
        assertCode("SESSION_CHANGED", oldScreen.getSummary("adult", 7))
        assertCode("SESSION_CHANGED", oldScreen.getTreatmentSummary("adult", "treatment", 7))
        assertCode("SESSION_CHANGED", oldScreen.getRecommendations("adult", 7))
        val adult = SessionOwner("adult", "OLDER_ADULT")
        store.value = session(adult)
        val adultScreen =
            SessionScopedAnalyticsRepositories(store, adult, { error("API must not open") }) { now }
        assertCode("ACCESS_DENIED", adultScreen.getSummary("another-adult", 7))
        assertCode(
            "ACCESS_DENIED",
            adultScreen.getTreatmentSummary("another-adult", "treatment", 7),
        )
        assertCode("ACCESS_DENIED", adultScreen.getRecommendations("another-adult", 7))
    }

    @Test
    fun eachOperationUsesItsCapturedCredentialsAndDiscardsLateResults() = runBlocking {
        for (operation in 0..2) {
            val initial = session()
            val store = Store(initial)
            var token: String? = null
            val repository =
                SessionScopedAnalyticsRepositories(
                    store,
                    owner,
                    { captured ->
                        token = captured.accessToken
                        Proxy.newProxyInstance(
                            AnalyticsApiService::class.java.classLoader,
                            arrayOf(AnalyticsApiService::class.java),
                        ) { _, _, _ ->
                            store.value = session(SessionOwner("other", "CAREGIVER"), "second")
                            Response.success<String>(204, null)
                        } as AnalyticsApiService
                    },
                ) {
                    now
                }
            val result =
                when (operation) {
                    0 -> repository.getSummary("adult", 7)
                    1 -> repository.getRecommendations("adult", 7)
                    else -> repository.getTreatmentSummary("adult", "treatment", 7)
                }
            assertCode("SESSION_CHANGED", result)
            assertEquals("first", token)
            assertEquals("second", store.value?.accessToken)
        }
    }

    @Test
    fun caregiverAndAdultCanReadWithAnUnchangedUsableSession() = runBlocking {
        for (account in listOf(owner, SessionOwner("adult", "OLDER_ADULT"))) {
            val store = Store(session(account))
            var calls = 0
            val repository =
                SessionScopedAnalyticsRepositories(
                    store,
                    account,
                    {
                        Proxy.newProxyInstance(
                            AnalyticsApiService::class.java.classLoader,
                            arrayOf(AnalyticsApiService::class.java),
                        ) { _, _, _ ->
                            calls++
                            Response.success<String>(204, null)
                        } as AnalyticsApiService
                    },
                ) {
                    now
                }
            assertEquals(AppResult.Success(null), repository.getSummary("adult", 7))
            assertEquals(AppResult.Success(null), repository.getRecommendations("adult", 7))
            assertEquals(2, calls)
        }
    }

    private fun assertCode(code: String, result: AppResult<*>) =
        assertEquals(code, (result as AppResult.Failure).code)

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
