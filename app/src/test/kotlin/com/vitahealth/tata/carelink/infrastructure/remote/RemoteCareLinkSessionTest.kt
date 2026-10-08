package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.shared.application.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteCareLinkSessionTest {
    private class RecordingSessionStore : SessionStore {
        var token: String? = null
        override fun save(accessToken: String, expiresAt: String) { token = accessToken }
        override fun accessToken(): String? = token
        override fun clear() { token = null }
    }
    private class Api : CareLinkApiService {
        private fun link(token: String, confirmed: Boolean) = CareLinkResponse("link", "caregiver", "adult", if (confirmed) "CONFIRMED" else "PENDING", null, null, "2026-10-07T00:00:00Z", confirmed, null, if (confirmed) "2026-10-07T00:00:00Z" else null, token, "2099-01-01T00:00:00Z")
        override suspend fun acceptLink(request: AcceptCareLinkRequest) = Response.success(link("setup-token", false))
        override suspend fun registerConsent(careLinkId: String, request: RegisterConsentRequest) = Response.success(link("adult-token", true))
        override suspend fun getOlderAdult(olderAdultId: String): Response<OlderAdultProfileResponse> = throw UnsupportedOperationException()
    }
    @Test fun consentReplacesRestrictedSetupSessionWithAdultSession() = runBlocking {
        val sessions = RecordingSessionStore()
        val repository = RemoteCareLinkRepository(Api(), sessions)
        assertTrue(repository.acceptLink("caregiver", "123456") is AppResult.Success)
        assertEquals("setup-token", sessions.token)
        assertTrue(repository.registerConsent("link", true) is AppResult.Success)
        assertEquals("adult-token", sessions.token)
    }
}
