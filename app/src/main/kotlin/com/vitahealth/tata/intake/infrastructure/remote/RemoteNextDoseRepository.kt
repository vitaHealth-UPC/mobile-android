package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.NextDoseRepository
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

class RemoteNextDoseRepository(
    private val api: IntakeApiService,
) : NextDoseRepository {
    override suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?> {
        return try {
            val response = api.getNextDose(olderAdultId)
            if (response.code() == 204) {
                AppResult.Success(null)
            } else {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    map(body)
                } else {
                    AppResult.Failure(
                        message = "No pudimos consultar la próxima toma.",
                        code = when (response.code()) {
                            404 -> "OLDER_ADULT_NOT_FOUND"
                            else -> "REQUEST_FAILED"
                        },
                    )
                }
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private fun map(body: IntakeResponse): AppResult<NextDoseReadModel> {
        val scheduledAt = runCatching { Instant.parse(body.scheduledAt) }.getOrNull()
            ?: return AppResult.Failure(
                message = "La programación de la toma no es válida.",
                code = "INVALID_SCHEDULE",
            )
        val status = runCatching { DoseStatus.valueOf(body.status.uppercase()) }.getOrNull()
            ?: return AppResult.Failure(
                message = "El estado de la toma no es válido.",
                code = "INVALID_DOSE_STATUS",
            )

        return AppResult.Success(
            NextDoseReadModel(
                id = body.id,
                treatmentId = body.treatmentId,
                medicationId = body.medicationId,
                olderAdultId = body.olderAdultId,
                medicationName = body.medicationName,
                dose = body.dose,
                instructions = body.instructions.orEmpty(),
                scheduledAt = scheduledAt,
                status = status,
            ),
        )
    }
}
