package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Confirms a dose through the application command and returns its resulting detail. */
interface DoseConfirmationRepository {
    suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel>
}
