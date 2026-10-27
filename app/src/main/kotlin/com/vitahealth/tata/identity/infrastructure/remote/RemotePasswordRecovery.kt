package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.application.ports.PasswordRecovery
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RequestPasswordRecoveryBody(val email: String) {
    override fun toString() = "RequestPasswordRecoveryBody[REDACTED]"
}

data class CompletePasswordRecoveryBody(
    val email: String,
    val code: String,
    val newPassword: String,
) {
    override fun toString() = "CompletePasswordRecoveryBody[REDACTED]"
}

interface PasswordRecoveryApi {
    @POST("api/v1/password-recovery-requests")
    suspend fun request(@Body body: RequestPasswordRecoveryBody): Response<Unit>

    @POST("api/v1/password-recoveries")
    suspend fun complete(@Body body: CompletePasswordRecoveryBody): Response<Unit>
}

/** Uses an anonymous API client; successful reset never implies authenticated access. */
class RemotePasswordRecovery(private val api: PasswordRecoveryApi) : PasswordRecovery {
    override suspend fun request(email: String) =
        exchange(202) { api.request(RequestPasswordRecoveryBody(email.trim())) }

    override suspend fun complete(email: String, code: String, newPassword: String) =
        exchange(204) {
            api.complete(CompletePasswordRecoveryBody(email.trim(), code, newPassword))
        }

    private suspend fun exchange(
        expected: Int,
        call: suspend () -> Response<Unit>,
    ): AppResult<Unit> =
        try {
            val response = call()
            when {
                response.code() == expected -> AppResult.Success(Unit)
                response.isSuccessful ->
                    AppResult.Failure("Respuesta no válida.", code = "INVALID_RESPONSE")
                else ->
                    AppResult.Failure(
                        "No pudimos completar la recuperación.",
                        code = response.problemCode(),
                    )
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (io: IOException) {
            AppResult.Failure("Sin conexión.", cause = io, code = "NETWORK_UNAVAILABLE")
        } catch (invalid: RuntimeException) {
            AppResult.Failure("Respuesta no válida.", cause = invalid, code = "INVALID_RESPONSE")
        }
}
