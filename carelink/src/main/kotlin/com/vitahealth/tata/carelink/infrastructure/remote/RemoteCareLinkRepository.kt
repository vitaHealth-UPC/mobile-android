package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.carelink.application.CareLinkRepository
import com.vitahealth.tata.carelink.domain.model.CareLink
import com.vitahealth.tata.carelink.domain.model.CareLinkStatus
import com.vitahealth.tata.carelink.domain.model.OlderAdultProfile
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate

class RemoteCareLinkRepository(
    private val api: CareLinkApiService,
) : CareLinkRepository {
    override suspend fun acceptLink(caregiverId: String, code: String): AppResult<CareLink> =
        requestCareLink {
            api.acceptLink(
                AcceptCareLinkRequest(
                    caregiverId = caregiverId,
                    code = code.trim().uppercase(),
                ),
            )
        }

    override suspend fun getOlderAdult(olderAdultId: String): AppResult<OlderAdultProfile> {
        return try {
            val response = api.getOlderAdult(olderAdultId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(
                    OlderAdultProfile(
                        id = body.id,
                        fullName = body.fullName,
                        birthDate = LocalDate.parse(body.birthDate),
                        emergencyContactName = body.emergencyContactName,
                        emergencyContactRelationship = body.emergencyContactRelationship,
                        emergencyContactPhone = body.emergencyContactPhone,
                    ),
                )
            } else {
                val (message, code) = when (response.code()) {
                    404 -> "No encontramos el perfil del adulto mayor." to "OLDER_ADULT_NOT_FOUND"
                    else -> "No pudimos cargar el perfil vinculado." to "REQUEST_FAILED"
                }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private suspend fun requestCareLink(
        call: suspend () -> Response<CareLinkResponse>,
    ): AppResult<CareLink> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(
                    CareLink(
                        id = body.id,
                        caregiverId = body.caregiverId,
                        olderAdultId = body.olderAdultId,
                        status = runCatching { CareLinkStatus.valueOf(body.status) }
                            .getOrDefault(CareLinkStatus.PENDING),
                        codeExpiresAt = body.codeExpiresAt.toInstantOrNull(),
                        codeUsedAt = body.codeUsedAt.toInstantOrNull(),
                        consentGranted = body.consentGranted,
                        consentRecordedAt = body.consentRecordedAt.toInstantOrNull(),
                        confirmedAt = body.confirmedAt.toInstantOrNull(),
                    ),
                )
            } else {
                val (message, code) = when (response.code()) {
                    400 -> "El código de vinculación no es válido." to "INVALID_LINKING_CODE"
                    403 -> "Tu cuenta todavía no está habilitada para vincular." to "ACCOUNT_NOT_ENABLED"
                    404 -> "No encontramos la solicitud de vinculación." to "CARE_LINK_NOT_FOUND"
                    410 -> "El código venció o ya fue utilizado." to "LINKING_CODE_EXPIRED_OR_USED"
                    else -> "No pudimos completar la vinculación." to "REQUEST_FAILED"
                }
                AppResult.Failure(message = message, code = code)
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión disponible.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private fun String?.toInstantOrNull(): Instant? =
        this?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }
}
