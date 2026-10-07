package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.treatment.application.MedicationManagementRepository
import com.vitahealth.tata.treatment.application.commands.DeactivateMedicationCommand
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.shared.common.result.AppResult

class DeactivateMedicationCommandHandler(
    private val repository: MedicationManagementRepository,
) {
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
