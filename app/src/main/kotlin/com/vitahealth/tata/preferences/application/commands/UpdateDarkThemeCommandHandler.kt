package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.application.ports.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.readmodels.PreferenceUpdate
import com.vitahealth.tata.shared.common.result.AppResult

class UpdateDarkThemeCommandHandler(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(command: UpdateDarkThemeCommand): AppResult<PreferenceUpdate> {
        val userId = command.userId.trim()
        if (userId.isBlank()) {
            return AppResult.Failure(
                message = "The user could not be identified.",
                code = "INVALID_USER_REFERENCE",
            )
        }
        return repository.updateDarkTheme(userId, command.enabled)
    }
}
