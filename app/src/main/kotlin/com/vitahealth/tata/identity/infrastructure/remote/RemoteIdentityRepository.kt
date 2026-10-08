package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.application.SessionStore
import com.vitahealth.tata.shared.application.NoSessionStore
import kotlinx.coroutines.CancellationException
import com.vitahealth.tata.identity.application.IdentityRepository
import com.vitahealth.tata.identity.domain.model.AccountStatus
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteIdentityRepository(
    private val api: IdentityApiService,
    private val sessions: SessionStore = NoSessionStore,
) : IdentityRepository {
    override suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String,
    ): AppResult<CaregiverAccount> = request {
        api.register(RegisterAccountRequest(name, email, password))
    }

    override suspend fun verifyEmail(email: String, code: String): AppResult<CaregiverAccount> = request {
        api.verify(VerifyEmailRequest(email, code))
    }

    override suspend fun requestNewVerification(email: String): AppResult<CaregiverAccount> = request {
        api.requestNewVerification(CreateEmailVerificationRequest(email))
    }

    private suspend fun request(call: suspend () -> retrofit2.Response<AccountResponse>): AppResult<CaregiverAccount> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.accessToken != null && body.expiresAt != null) sessions.save(body.accessToken, body.expiresAt)
                AppResult.Success(
                    CaregiverAccount(
                        id = body.id,
                        name = body.name,
                        email = body.email,
                        status = runCatching { AccountStatus.valueOf(body.status) }
                            .getOrDefault(AccountStatus.PENDING_VERIFICATION),
                    ),
                )
            } else {
                val (message, code) = when (response.code()) {
                    409 -> "This e-mail is already registered" to "DUPLICATE_EMAIL"
                    410 -> "The verification code has expired" to "VERIFICATION_EXPIRED"
                    401 -> "The verification code is invalid" to "INVALID_VERIFICATION"
                    404 -> "The account was not found" to "ACCOUNT_NOT_FOUND"
                    403 -> "The account cannot request a new verification code" to "ACCOUNT_NOT_ACTIVE"
                    else -> "We could not complete the request" to "REQUEST_FAILED"
                }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "Network unavailable",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }
}
