package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.domain.model.AccountStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response

class RemoteIdentityRepositoryTest {
    private class Api(var response: () -> Response<AccountResponse>) : IdentityApiService {
        override suspend fun register(request: RegisterAccountRequest) = response()
        override suspend fun verify(request: VerifyEmailRequest) = response()
        override suspend fun requestNewVerification(request: CreateEmailVerificationRequest) = response()
    }

    private fun httpError(status: Int) = Response.error<AccountResponse>(status, "{}".toResponseBody())
    private fun ok(status: String = "PENDING_VERIFICATION") =
        Response.success(AccountResponse("account-1", "Diego", "diego@example.com", status))

    private fun codeFor(status: Int): String? {
        val result = runBlocking {
            RemoteIdentityRepository(Api { httpError(status) }).registerCaregiver("Diego", "d@e.com", "password1")
        }
        return (result as? AppResult.Failure)?.code
    }

    @Test
    fun aSuccessfulAnswerIsMappedToTheCaregiverAccount() = runBlocking {
        val result = RemoteIdentityRepository(Api { ok("ACTIVE") }).registerCaregiver("Diego", "d@e.com", "password1")

        val account = (result as AppResult.Success).value
        assertEquals("account-1", account.id)
        assertEquals("Diego", account.name)
        assertEquals("diego@example.com", account.email)
        assertEquals(AccountStatus.ACTIVE, account.status)
    }

    @Test
    fun anUnknownAccountStatusIsTreatedAsPendingVerification() = runBlocking {
        val result = RemoteIdentityRepository(Api { ok("SUSPENDED") }).registerCaregiver("Diego", "d@e.com", "password1")

        assertEquals(AccountStatus.PENDING_VERIFICATION, (result as AppResult.Success).value.status)
    }

    @Test
    fun eachHttpStatusIsTranslatedToTheCodeTheScreensUnderstand() {
        assertEquals("DUPLICATE_EMAIL", codeFor(409))
        assertEquals("VERIFICATION_EXPIRED", codeFor(410))
        assertEquals("INVALID_VERIFICATION", codeFor(401))
        assertEquals("ACCOUNT_NOT_FOUND", codeFor(404))
        assertEquals("ACCOUNT_NOT_ACTIVE", codeFor(403))
        assertEquals("REQUEST_FAILED", codeFor(500))
        assertEquals("REQUEST_FAILED", codeFor(400))
    }

    @Test
    fun aNetworkErrorIsReportedAsUnavailable() = runBlocking {
        val result = RemoteIdentityRepository(Api { throw IOException("no route") })
            .verifyEmail("d@e.com", "123456")

        val failure = result as AppResult.Failure
        assertEquals("NETWORK_UNAVAILABLE", failure.code)
        assertEquals("no route", failure.cause?.message)
    }

    @Test
    fun anEmptyBodyOnASuccessfulCallIsAFailureNotACrash() = runBlocking {
        val result = RemoteIdentityRepository(Api { Response.success<AccountResponse>(null) })
            .requestNewVerification("d@e.com")

        assertEquals("REQUEST_FAILED", (result as AppResult.Failure).code)
        assertNull(result.cause)
    }
}
