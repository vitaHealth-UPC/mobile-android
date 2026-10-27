package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.repositories.TreatmentRepository

/** Validates required medication fields before requesting registration. */
class RegisterMedicationCommandHandler(private val repository: TreatmentRepository) {
    suspend operator fun invoke(command: RegisterMedicationCommand): AppResult<Medication> {
        val caregiverId = command.caregiverId.trim()
        val olderAdultId = command.olderAdultId.trim()
        val name = command.name.trim()
        val presentation = command.presentation.trim()

        if (
            caregiverId.isBlank() ||
                olderAdultId.isBlank() ||
                name.isBlank() ||
                presentation.isBlank()
        ) {
            return AppResult.Failure(
                message = "Completa los datos obligatorios del medicamento.",
                code = "REQUIRED_FIELDS_MISSING",
            )
        }

        return repository.registerMedication(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            name = name,
            presentation = presentation,
        )
    }
}
