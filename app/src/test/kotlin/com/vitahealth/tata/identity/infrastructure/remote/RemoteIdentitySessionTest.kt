package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.application.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteIdentitySessionTest {
    private class RecordingSessionStore : SessionStore {
        var token: String? = null
        var expiry: String? = null
        override fun save(accessToken: String, expiresAt: String) { token = accessToken; expiry = expiresAt }
        override fun accessToken(): String? = token
        override fun clear() { token = null; expiry = null }
    }
    private open class Api : IdentityApiService {
        override suspend fun register(request: RegisterAccountRequest) = Response.success(AccountResponse("id", "Name", "a@b.com", "PENDING_VERIFICATION"))
        override suspend fun verify(request: VerifyEmailRequest) = Response.success(AccountResponse("id", "Name", "a@b.com", "ACTIVE", "verified-token", "2099-01-01T00:00:00Z"))
        override suspend fun requestNewVerification(request: CreateEmailVerificationRequest) = Response.success(AccountResponse("id", "Name", "a@b.com", "PENDING_VERIFICATION"))
    }
    @Test fun verificationPersistsSessionBeforeReturningSuccess() = runBlocking {
        val sessions = RecordingSessionStore()
        val result = RemoteIdentityRepository(Api(), sessions).verifyEmail("a@b.com", "123456")
        assertTrue(result is AppResult.Success)
        assertEquals("verified-token", sessions.token)
        assertEquals("2099-01-01T00:00:00Z", sessions.expiry)
    }
    @Test fun registrationDoesNotOverwriteAnExistingSessionWithNullCredentials() = runBlocking {
        val sessions = RecordingSessionStore().apply { save("existing", "2099-01-01T00:00:00Z") }
        RemoteIdentityRepository(Api(), sessions).registerCaregiver("Name", "a@b.com", "password")
        assertEquals("existing", sessions.token)
    }
    @Test fun cancellationPropagates() = runBlocking {
        val api = object : Api() { override suspend fun verify(request: VerifyEmailRequest): Response<AccountResponse> = throw CancellationException("cancelled") }
        try { RemoteIdentityRepository(api).verifyEmail("a@b.com", "123456"); fail("Cancellation was swallowed") }
        catch (_: CancellationException) { }
    }
}
