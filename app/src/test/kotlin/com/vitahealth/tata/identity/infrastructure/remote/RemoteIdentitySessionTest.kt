package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
class RemoteIdentitySessionTest {
    @Test
    fun verificationResponseCannotReplaceAnAccountThatLoggedInDuringTheRequest() = runBlocking {
        val sessions = RecordingSessionStore()
        val api =
            object : Api() {
                override suspend fun verify(
                    request: VerifyEmailRequest
                ): Response<AccountResponse> {
                    sessions.save(
                        "new-login",
                        "2099-01-01T00:00:00Z",
                        SessionOwner("other", "CAREGIVER"),
                    )
                    return Response.success(
                        AccountResponse(
                            "id",
                            "Name",
                            "a@b.com",
                            "ACTIVE",
                            "old-response",
                            "2099-01-01T00:00:00Z",
                        )
                    )
                }
            }
        val result = RemoteIdentityRepository(api, sessions).verifyEmail("a@b.com", "123456")
        assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
        assertEquals("new-login", sessions.token)
        assertEquals(SessionOwner("other", "CAREGIVER"), sessions.owner)
    }

    private class RecordingSessionStore : SessionStore {
        var token: String? = null
        var owner: SessionOwner? = null

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            save(accessToken, expiresAt)
            this.owner = owner
        }

        override fun session(): StoredSession? =
            token?.let { StoredSession(it, Instant.parse("2099-01-01T00:00:00Z"), owner) }

        var expiry: String? = null

        override fun save(accessToken: String, expiresAt: String) {
            token = accessToken
            expiry = expiresAt
        }

        override fun accessToken(): String? = token

        override fun clear() {
            token = null
            expiry = null
            owner = null
        }
    }

    private open class Api : IdentityApiService {
        override suspend fun register(request: RegisterAccountRequest) =
            Response.success(AccountResponse("id", "Name", "a@b.com", "PENDING_VERIFICATION"))

        override suspend fun verify(request: VerifyEmailRequest) =
            Response.success(
                AccountResponse(
                    "id",
                    "Name",
                    "a@b.com",
                    "ACTIVE",
                    "verified-token",
                    "2099-01-01T00:00:00Z",
                )
            )

        override suspend fun requestNewVerification(request: CreateEmailVerificationRequest) =
            Response.success(AccountResponse("id", "Name", "a@b.com", "PENDING_VERIFICATION"))
    }

    @Test
    fun verificationPersistsSessionBeforeReturningSuccess() = runBlocking {
        val sessions = RecordingSessionStore()
        val result = RemoteIdentityRepository(Api(), sessions).verifyEmail("a@b.com", "123456")
        assertTrue(result is AppResult.Success)
        assertEquals("verified-token", sessions.token)
        assertEquals(SessionOwner("id", "CAREGIVER"), sessions.owner)
        assertEquals("2099-01-01T00:00:00Z", sessions.expiry)
    }

    @Test
    fun registrationDoesNotOverwriteAnExistingSessionWithNullCredentials() = runBlocking {
        val sessions = RecordingSessionStore().apply { save("existing", "2099-01-01T00:00:00Z") }
        RemoteIdentityRepository(Api(), sessions).registerCaregiver("Name", "a@b.com", "password")
        assertEquals("existing", sessions.token)
    }

    @Test
    fun cancellationPropagates() = runBlocking {
        val api =
            object : Api() {
                override suspend fun verify(
                    request: VerifyEmailRequest
                ): Response<AccountResponse> = throw CancellationException("cancelled")
            }
        try {
            RemoteIdentityRepository(api).verifyEmail("a@b.com", "123456")
            fail("Cancellation was swallowed")
        } catch (_: CancellationException) {}
    }

    @Test
    fun malformedAccountNeverOverwritesExistingCredentials() = runBlocking {
        val responses =
            listOf(
                AccountResponse(
                    "id",
                    "Name",
                    "a@b.com",
                    "UNKNOWN",
                    "wrong",
                    "2099-01-01T00:00:00Z",
                ),
                AccountResponse("id", "Name", "a@b.com", "ACTIVE", "wrong", null),
                AccountResponse("id", "Name", "a@b.com", "ACTIVE", "wrong", "invalid"),
                AccountResponse("", "Name", "a@b.com", "ACTIVE", "wrong", "2099-01-01T00:00:00Z"),
            )
        for (body in responses) {
            val sessions =
                RecordingSessionStore().apply { save("existing", "2099-01-01T00:00:00Z") }
            val api =
                object : Api() {
                    override suspend fun verify(request: VerifyEmailRequest) =
                        Response.success(body)
                }
            val result = RemoteIdentityRepository(api, sessions).verifyEmail("a@b.com", "123456")
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
            assertEquals("existing", sessions.token)
        }
    }
}
