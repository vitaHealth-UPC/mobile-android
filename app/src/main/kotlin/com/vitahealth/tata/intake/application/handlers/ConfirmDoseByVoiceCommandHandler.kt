package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.VoiceConfirmationRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class ConfirmDoseByVoiceCommandHandler(private val repository: VoiceConfirmationRepository) {
    suspend operator fun invoke(command: ConfirmDoseByVoiceCommand): AppResult<VoiceConfirmationReadModel> {
        if (command.intakeId.isBlank()) return AppResult.Failure("No se pudo identificar la toma.", code = "INVALID_INTAKE_REFERENCE")
        if (command.audio.isEmpty() || command.audio.size > 5 * 1024 * 1024) return AppResult.Failure("La grabación no es válida.", code = "INVALID_AUDIO")
        if (!command.contentType.startsWith("audio/") || command.language.isBlank()) return AppResult.Failure("La grabación no es válida.", code = "INVALID_AUDIO")
        return repository.confirm(command)
    }
}
