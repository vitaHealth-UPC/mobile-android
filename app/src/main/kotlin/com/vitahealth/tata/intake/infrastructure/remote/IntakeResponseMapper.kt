package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

internal fun mapIntakeDetail(body: IntakeResponse): AppResult<DoseDetailReadModel> {
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
            confirmedAt = body.confirmedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
        ),
    )
}

