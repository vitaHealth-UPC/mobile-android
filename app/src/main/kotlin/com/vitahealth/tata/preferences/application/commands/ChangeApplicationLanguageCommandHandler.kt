package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.application.ports.ApplicationLanguageRepository
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.shared.common.result.AppResult

data class ChangeApplicationLanguageCommand(val userId: String, val language: AppLanguage)

/** Keeps UI and infrastructure separate while validating the target identity. */
class ChangeApplicationLanguageCommandHandler(
    private val repository: ApplicationLanguageRepository,
) {
    suspend operator fun invoke(command: ChangeApplicationLanguageCommand):
        AppResult<AppLanguage> =
        if (command.userId.isBlank()) {
            AppResult.Failure("Usuario inválido.", code = "INVALID_USER_REFERENCE")
        } else repository.change(command.userId.trim(), command.language)
}
