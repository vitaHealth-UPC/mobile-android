package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommand
import com.vitahealth.tata.treatment.application.commands.TreatmentLifecycleAction
import com.vitahealth.tata.treatment.domain.model.Treatment

class ChangeTreatmentStatusCommandHandler(
    private val repository: TreatmentLifecycleRepository,
) {
    suspend operator fun invoke(command: ChangeTreatmentStatusCommand): AppResult<Treatment> {
        val caregiverId = command.caregiverId.trim()
        val treatmentId = command.treatmentId.trim()
        if (caregiverId.isBlank() || treatmentId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar el tratamiento.",
                code = "INVALID_TREATMENT_REFERENCE",
            )
        }

        return when (command.action) {
            TreatmentLifecycleAction.ACTIVATE -> repository.activateTreatment(caregiverId, treatmentId)
            TreatmentLifecycleAction.PAUSE -> repository.pauseTreatment(caregiverId, treatmentId)
            TreatmentLifecycleAction.RESUME -> repository.resumeTreatment(caregiverId, treatmentId)
        }
    }
}
