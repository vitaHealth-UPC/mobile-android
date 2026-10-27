package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.repositories.MedicationManagementRepository

/** Validates medication identity before requesting deactivation. */
class DeactivateMedicationCommandHandler(private val repository: MedicationManagementRepository) {
    suspend operator fun invoke(command: DeactivateMedicationCommand): AppResult<Medication> {
        val caregiverId = command.caregiverId.trim()
        val medicationId = command.medicationId.trim()

        if (caregiverId.isBlank() || medicationId.isBlank()) {
            return AppResult.Failure(
                message = "The medication could not be identified.",
                code = "INVALID_MEDICATION_REFERENCE",
            )
        }
        return repository.deactivateMedication(caregiverId, medicationId)
    }
}
