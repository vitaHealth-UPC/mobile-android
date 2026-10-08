package com.vitahealth.tata.intake.application

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface DoseConfirmationRepository {
    suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel>
}
