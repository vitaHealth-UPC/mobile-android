package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.application.commands.CreateTreatmentCommand
import com.vitahealth.tata.treatment.domain.model.Treatment

class CreateTreatmentCommandHandler(
    private val repository: TreatmentRepository,
) {
    suspend operator fun invoke(command: CreateTreatmentCommand): AppResult<Treatment> {
        val caregiverId = command.caregiverId.trim()
        val olderAdultId = command.olderAdultId.trim()
        val name = command.name.trim()

        if (caregiverId.isBlank() || olderAdultId.isBlank() || name.isBlank()) {
            return AppResult.Failure(
                message = "Completa el nombre del tratamiento.",
                code = "REQUIRED_FIELDS_MISSING",
            )
        }

        return repository.createTreatment(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            name = name,
        )
    }
}
