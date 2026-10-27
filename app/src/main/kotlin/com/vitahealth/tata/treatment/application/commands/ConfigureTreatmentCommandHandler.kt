package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.AdministrationInstructions
import com.vitahealth.tata.treatment.domain.model.valueobjects.AdministrationSchedule
import com.vitahealth.tata.treatment.domain.model.valueobjects.Dosage
import com.vitahealth.tata.treatment.domain.model.valueobjects.Frequency
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Validates a regimen with the same domain values used by the treatment wizard. */
class ConfigureTreatmentCommandHandler(private val repository: TreatmentLifecycleRepository) {
    private val timeFormatter =
        DateTimeFormatter.ofPattern("H:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)

    suspend operator fun invoke(command: ConfigureTreatmentCommand): AppResult<Treatment> {
        val caregiverId = command.caregiverId.trim()
        val treatmentId = command.treatmentId.trim()
        val medicationId = command.medicationId.trim()
        val dose = Dosage.parse(command.dose)
        val frequency = Frequency.parse(command.frequency)
        val instructions = AdministrationInstructions.parse(command.instructions)
        val times = command.scheduledTimes.map(String::trim).filter(String::isNotBlank)
        val schedule =
            try {
                AdministrationSchedule.create(times.map { LocalTime.parse(it, timeFormatter) })
            } catch (_: DateTimeParseException) {
                null
            }
        if (
            caregiverId.isBlank() ||
                treatmentId.isBlank() ||
                medicationId.isBlank() ||
                dose == null ||
                frequency == null ||
                instructions == null ||
                schedule == null ||
                command.reminderLeadMinutes !in 0..1440 ||
                (command.followUpDelayMinutes != null && command.followUpDelayMinutes !in 0..1440)
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
            dose = dose.value,
            frequency = frequency.value,
            scheduledTimes = schedule.times.map(LocalTime::toString),
            instructions = instructions.value,
            reminderLeadMinutes = command.reminderLeadMinutes,
            followUpDelayMinutes = command.followUpDelayMinutes,
        )
    }
}
