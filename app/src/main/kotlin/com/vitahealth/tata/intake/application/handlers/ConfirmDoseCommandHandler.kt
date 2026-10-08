package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class ConfirmDoseCommandHandler(private val repository: DoseConfirmationRepository) {
    suspend operator fun invoke(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel> {
        val id = command.intakeId.trim()
        if (id.isBlank()) return AppResult.Failure("No se pudo identificar la toma.", code = "INVALID_INTAKE_REFERENCE")
        return repository.confirm(command.copy(intakeId = id))
    }
}
