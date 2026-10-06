package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.application.IdentityRepository
import com.vitahealth.tata.identity.domain.model.AccountStatus
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteIdentityRepository(
    private val api: IdentityApiService,
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

    private suspend fun request(call: suspend () -> retrofit2.Response<AccountResponse>): AppResult<CaregiverAccount> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
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
                val message = when (response.code()) {
                    409 -> "This e-mail is already registered"
                    410 -> "The verification code has expired"
                    401 -> "The verification code is invalid"
                    else -> "We could not complete the request"
                }
                AppResult.Failure(message)
            }
        } catch (exception: Exception) {
            AppResult.Failure("Network unavailable", exception)
        }
    }
}
