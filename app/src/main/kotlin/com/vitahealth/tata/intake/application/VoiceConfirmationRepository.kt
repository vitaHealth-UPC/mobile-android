package com.vitahealth.tata.intake.application

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface VoiceConfirmationRepository {
    suspend fun confirm(command: ConfirmDoseByVoiceCommand): AppResult<VoiceConfirmationReadModel>
}
