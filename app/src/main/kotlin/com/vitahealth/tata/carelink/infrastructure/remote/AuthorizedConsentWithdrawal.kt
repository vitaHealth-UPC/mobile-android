package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

data class ConsentSessionResponse(
    val subjectId: String?,
    val role: String?,
    val careLinkId: String?,
)

interface ConsentWithdrawalApi {
    @GET("api/v1/sessions/current") suspend fun current(): Response<ConsentSessionResponse>

    @DELETE("api/v1/care-links/{careLinkId}/consent")
    suspend fun withdraw(@Path("careLinkId") careLinkId: String): Response<Unit>
}

/**
 * Resolves the relationship from the verified session before requesting irreversible withdrawal.
 */
class AuthorizedConsentWithdrawal(
    private val sessions: SessionStore,
    private val baseUrl: String,
    private val now: () -> Instant = Instant::now,
) : ConsentWithdrawal {
    override suspend fun withdraw(olderAdultId: String): AppResult<Unit> {
        val captured =
            sessions.session()
                ?: return AppResult.Failure("Sesión requerida.", code = "AUTHENTICATION_REQUIRED")
        if (captured.owner?.subjectId != olderAdultId || captured.owner.role != "OLDER_ADULT")
            return AppResult.Failure("Cuenta no autorizada.", code = "RESOURCE_ACCESS_DENIED")
        if (!captured.expiresAt.isAfter(now()))
            return AppResult.Failure("Sesión expirada.", code = "AUTHENTICATION_REQUIRED")
        fun current() = sessions.isCurrent(captured) && captured.expiresAt.isAfter(now())
        return try {
            val retrofit = ApiClientFactory.create(baseUrl) { captured.accessToken }
            val identity = retrofit.create(ConsentWithdrawalApi::class.java).current()
            currentCoroutineContext().ensureActive()
            if (!current()) return AppResult.Failure("Sesión cambiada.", code = "SESSION_CHANGED")
            if (!identity.isSuccessful) return failure(identity.code())
            val body = identity.body()
            if (
                body == null ||
                    body.subjectId != olderAdultId ||
                    body.role != "OLDER_ADULT" ||
                    body.careLinkId.isNullOrBlank()
            )
                return AppResult.Failure("Vínculo no identificado.", code = "INVALID_RESPONSE")
            val receipt =
                retrofit.create(ConsentWithdrawalApi::class.java).withdraw(body.careLinkId)
            currentCoroutineContext().ensureActive()
            if (!current()) return AppResult.Failure("Sesión cambiada.", code = "SESSION_CHANGED")
            if (receipt.code() != 204)
                return if (receipt.isSuccessful)
                    AppResult.Failure("Respuesta no válida.", code = "INVALID_RESPONSE")
                else failure(receipt.code())
            AppResult.Success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (io: IOException) {
            AppResult.Failure(
                "No pudimos confirmar el retiro. Comprueba tu conexión.",
                cause = io,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (invalid: RuntimeException) {
            AppResult.Failure("Respuesta no válida.", cause = invalid, code = "INVALID_RESPONSE")
        }
    }

    private fun failure(status: Int) =
        AppResult.Failure(
            "No pudimos retirar el consentimiento.",
            code =
                when (status) {
                    401 -> "AUTHENTICATION_REQUIRED"
                    403 -> "RESOURCE_ACCESS_DENIED"
                    404 -> "CARE_LINK_NOT_FOUND"
                    409 -> "INVALID_STATE"
                    else -> "REQUEST_FAILED"
                },
        )
}
