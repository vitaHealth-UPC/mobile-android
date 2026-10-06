package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

class RemoteDoseDetailRepository(
    private val api: IntakeApiService,
) : DoseDetailRepository {
    override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> =
        try {
            val response = api.getDoseDetail(intakeId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                map(body)
            } else {
                AppResult.Failure(
                    message = if (response.code() == 404) "No encontramos esta toma."
                    else "No pudimos consultar el detalle de la toma.",
                    code = if (response.code() == 404) "INTAKE_NOT_FOUND" else "REQUEST_FAILED",
                )
            }
        } catch (exception: Exception) {
            AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        }

    private fun map(body: IntakeResponse): AppResult<DoseDetailReadModel> {
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
            DoseDetailReadModel(
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
