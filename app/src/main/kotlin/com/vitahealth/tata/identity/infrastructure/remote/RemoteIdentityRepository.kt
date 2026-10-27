package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.identity.domain.model.entities.CaregiverAccount
import com.vitahealth.tata.identity.domain.model.valueobjects.AccountStatus
import com.vitahealth.tata.identity.domain.repositories.IdentityRepository
import com.vitahealth.tata.shared.infrastructure.security.NoSessionStore
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.Response
/** Maps account responses before accepting any optional session credentials. */
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

    override suspend fun verifyEmail(email: String, code: String): AppResult<CaregiverAccount> =
        request {
            api.verify(VerifyEmailRequest(email, code))
        }

    override suspend fun requestNewVerification(email: String): AppResult<CaregiverAccount> =
        request {
            api.requestNewVerification(CreateEmailVerificationRequest(email))
        }

    private suspend fun request(
        call: suspend () -> Response<AccountResponse>
    ): AppResult<CaregiverAccount> {
        return try {
            val expectedSession = sessions.session()
            val response = call()
            currentCoroutineContext().ensureActive()
            if (!sessions.isCurrent(expectedSession)) return sessionChanged()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                require(body.id.isNotBlank() && body.name.isNotBlank() && body.email.isNotBlank())
                val account =
                    CaregiverAccount(
                        id = body.id,
                        name = body.name,
                        email = body.email,
                        status = AccountStatus.valueOf(body.status),
                    )
                require((body.accessToken == null) == (body.expiresAt == null))
                if (body.accessToken != null && body.expiresAt != null) {
                    require(body.accessToken.isNotBlank())
                    val expiry = Instant.parse(body.expiresAt)
                    require(expiry.isAfter(Instant.now()))
                    if (
                        !sessions.replaceIfCurrent(
                            expectedSession,
                            StoredSession(
                                body.accessToken,
                                expiry,
                                SessionOwner(account.id, "CAREGIVER"),
                            ),
                        )
                    )
                        return sessionChanged()
                }
                AppResult.Success(account)
            } else {
                val (message, code) =
                    when (response.code()) {
                        409 -> "This e-mail is already registered" to "DUPLICATE_EMAIL"
                        410 -> "The verification code has expired" to "VERIFICATION_EXPIRED"
                        401 -> "The verification code is invalid" to "INVALID_VERIFICATION"
                        404 -> "The account was not found" to "ACCOUNT_NOT_FOUND"
                        403 ->
                            "The account cannot request a new verification code" to
                                "ACCOUNT_NOT_ACTIVE"
                        else -> "We could not complete the request" to "REQUEST_FAILED"
                    }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Invalid account response", code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure(
                message = "Network unavailable",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                message = "Invalid account response",
                cause = exception,
                code = "INVALID_RESPONSE",
            )
        }
    }

    private fun sessionChanged() = AppResult.Failure("Session changed", code = "SESSION_CHANGED")
}
