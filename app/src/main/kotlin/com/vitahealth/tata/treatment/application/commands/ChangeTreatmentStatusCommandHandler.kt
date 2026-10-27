package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository

/** Validates treatment references and dispatches the requested lifecycle transition. */
class ChangeTreatmentStatusCommandHandler(private val repository: TreatmentLifecycleRepository) {
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
            TreatmentLifecycleAction.ACTIVATE ->
                repository.activateTreatment(caregiverId, treatmentId)
            TreatmentLifecycleAction.PAUSE -> repository.pauseTreatment(caregiverId, treatmentId)
            TreatmentLifecycleAction.RESUME -> repository.resumeTreatment(caregiverId, treatmentId)
        }
    }
}
