package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.util.Locale

/** Validates transport facts before any repository can cache an intake. */
internal fun mapIntakeDetail(body: IntakeResponse): AppResult<DoseDetailReadModel> {
    if (
        listOf(
                body.id,
                body.treatmentId,
                body.medicationId,
                body.olderAdultId,
                body.medicationName,
                body.dose,
            )
            .any { it.isNullOrBlank() }
    )
        return invalidIntakeResponse()
    val scheduledAt =
        runCatching { Instant.parse(body.scheduledAt) }.getOrNull()
            ?: return AppResult.Failure(
                message = "La programación de la toma no es válida.",
                code = "INVALID_SCHEDULE",
            )
    val status =
        runCatching { DoseStatus.valueOf(body.status.uppercase(Locale.ROOT)) }.getOrNull()
            ?: return AppResult.Failure(
                message = "El estado de la toma no es válido.",
                code = "INVALID_DOSE_STATUS",
            )

    if (body.alreadyConfirmed && status != DoseStatus.CONFIRMED && status != DoseStatus.LATE) {
        return AppResult.Failure(
            "El resultado de la confirmación no es válido.",
            code = "INVALID_DOSE_STATUS",
        )
    }
    val confirmedAt =
        body.confirmedAt?.let { supplied ->
            runCatching { Instant.parse(supplied) }.getOrNull() ?: return invalidIntakeResponse()
        }
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
            confirmedAt = confirmedAt,
            alreadyConfirmed = body.alreadyConfirmed,
        )
    )
}
