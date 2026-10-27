package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Submits recorded voice evidence and returns the confirmation outcome. */
interface VoiceConfirmationRepository {
    suspend fun confirm(command: ConfirmDoseByVoiceCommand): AppResult<VoiceConfirmationReadModel>
}
