package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.treatment.application.MedicationManagementRepository
import com.vitahealth.tata.treatment.application.commands.UpdateMedicationCommand
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.shared.common.result.AppResult

class UpdateMedicationCommandHandler(
    private val repository: MedicationManagementRepository,
) {
    suspend operator fun invoke(command: UpdateMedicationCommand): AppResult<Medication> {
        val caregiverId = command.caregiverId.trim()
        val medicationId = command.medicationId.trim()
        val name = command.name.trim()
        val presentation = command.presentation.trim()

        if (caregiverId.isBlank() || medicationId.isBlank()) {
            return AppResult.Failure(
                message = "The medication could not be identified.",
                code = "INVALID_MEDICATION_REFERENCE",
            )
        }
        if (name.isBlank() || presentation.isBlank()) {
            return AppResult.Failure(
                message = "The name and the dose are required.",
                code = "REQUIRED_FIELDS_MISSING",
            )
        }
        return repository.updateMedication(caregiverId, medicationId, name, presentation)
    }
}
