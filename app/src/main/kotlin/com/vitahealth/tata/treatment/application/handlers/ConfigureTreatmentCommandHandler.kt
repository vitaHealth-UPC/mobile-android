package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommand
import com.vitahealth.tata.treatment.domain.model.Treatment
import java.time.LocalTime

class ConfigureTreatmentCommandHandler(
    private val repository: TreatmentLifecycleRepository,
) {
    suspend operator fun invoke(command: ConfigureTreatmentCommand): AppResult<Treatment> {
        val caregiverId = command.caregiverId.trim()
        val treatmentId = command.treatmentId.trim()
        val medicationId = command.medicationId.trim()
        val dose = command.dose.trim()
        val frequency = command.frequency.trim()
        val instructions = command.instructions.trim()
        val times = command.scheduledTimes
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

        val validTimes = times.isNotEmpty() && times.all {
            runCatching { LocalTime.parse(it) }.isSuccess
        }
        if (
            caregiverId.isBlank() ||
            treatmentId.isBlank() ||
            medicationId.isBlank() ||
            dose.isBlank() ||
            frequency.isBlank() ||
            !validTimes ||
            command.reminderLeadMinutes !in 0..1440
        ) {
            return AppResult.Failure(
                message = "Completa una pauta válida antes de activar el tratamiento.",
                code = "INVALID_TREATMENT_CONFIGURATION",
            )
        }

        return repository.configureTreatment(
            caregiverId = caregiverId,
            treatmentId = treatmentId,
            medicationId = medicationId,
            dose = dose,
            frequency = frequency,
            scheduledTimes = times,
            instructions = instructions,
            reminderLeadMinutes = command.reminderLeadMinutes,
        )
    }
}
