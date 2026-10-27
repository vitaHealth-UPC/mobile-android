package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentRepository

/** Validates creation input and requires the saved treatment to remain a draft. */
class CreateTreatmentCommandHandler(private val repository: TreatmentRepository) {
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

        return when (
            val result =
                repository.createTreatment(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    name = name,
                )
        ) {
            is AppResult.Success -> {
                if (result.value.status == TreatmentStatus.DRAFT) {
                    result
                } else {
                    AppResult.Failure(
                        message =
                            "El tratamiento debe crearse como borrador hasta completar su pauta.",
                        code = "INVALID_INITIAL_TREATMENT_STATE",
                    )
                }
            }
            is AppResult.Failure -> result
        }
    }
}
