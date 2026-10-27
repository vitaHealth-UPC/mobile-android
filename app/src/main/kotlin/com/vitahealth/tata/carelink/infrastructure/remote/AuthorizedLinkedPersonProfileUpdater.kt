package com.vitahealth.tata.carelink.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.carelink.application.ports.LinkedPersonProfileUpdater
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

data class UpdateLinkedAdultRequest(val fullName: String, val birthDate: String)
data class UpdatedLinkedAdultResponse(
    val id: String,
    val registeredByCaregiverId: String,
    val fullName: String,
    val birthDate: String,
)
interface LinkedPersonProfileApi {
    @PUT("api/v1/older-adults/{olderAdultId}")
    suspend fun update(
        @Path("olderAdultId") adultId: String,
        @Body request: UpdateLinkedAdultRequest,
    ): Response<UpdatedLinkedAdultResponse>
}

/** Uses the captured caregiver credential and rejects foreign/invalid mutation receipts. */
class AuthorizedLinkedPersonProfileUpdater(
    private val sessions: SessionStore,
    private val screenCaregiverId: String,
    private val baseUrl: String,
    private val now: () -> Instant = Instant::now,
) : LinkedPersonProfileUpdater {
    override suspend fun update(
        caregiverId: String, olderAdultId: String, name: String, birthDate: LocalDate
    ): AppResult<Unit> {
        val captured = sessions.session()
            ?: return AppResult.Failure("Sesion requerida.", code = "AUTHENTICATION_REQUIRED")
        val owner = captured.owner
        if (owner == null || !captured.expiresAt.isAfter(now()))
            return AppResult.Failure("Sesion expirada.", code = "AUTHENTICATION_REQUIRED")
        if (owner.role != "CAREGIVER" || owner.subjectId != screenCaregiverId ||
            caregiverId != screenCaregiverId)
            return AppResult.Failure("Cuenta no autorizada.", code = "RESOURCE_ACCESS_DENIED")
        return try {
            val api = ApiClientFactory.create(baseUrl) { captured.accessToken }
                .create(LinkedPersonProfileApi::class.java)
            val response = api.update(
                olderAdultId, UpdateLinkedAdultRequest(name, birthDate.toString())
            )
            currentCoroutineContext().ensureActive()
            if (!sessions.isCurrent(captured) || !captured.expiresAt.isAfter(now()))
                return AppResult.Failure("Sesion cambiada.", code = "SESSION_CHANGED")
            if (!response.isSuccessful) {
                return AppResult.Failure(
                    "No fue posible editar el perfil.",
                    code = when (response.code()) {
                        401 -> "AUTHENTICATION_REQUIRED"
                        403 -> "RESOURCE_ACCESS_DENIED"
                        404 -> "OLDER_ADULT_NOT_FOUND"
                        400 -> "VALIDATION_ERROR"
                        else -> "REQUEST_FAILED"
                    },
                )
            }
            val result = response.body()
            if (result == null || result.id != olderAdultId ||
                result.registeredByCaregiverId != caregiverId ||
                result.fullName != name || result.birthDate != birthDate.toString()) {
                AppResult.Failure(
                    "El servidor devolvio un perfil diferente.",
                    code = "INVALID_RESPONSE",
                )
            } else AppResult.Success(Unit)
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (io: IOException) {
            AppResult.Failure("Sin conexion.", cause = io, code = "NETWORK_UNAVAILABLE")
        } catch (invalid: RuntimeException) {
            AppResult.Failure("Respuesta invalida.", cause = invalid, code = "INVALID_RESPONSE")
        }
    }
}
