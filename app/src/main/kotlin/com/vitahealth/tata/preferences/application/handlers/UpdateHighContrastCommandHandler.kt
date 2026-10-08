package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.application.PreferenceUpdate
import com.vitahealth.tata.preferences.application.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.commands.UpdateHighContrastCommand
import com.vitahealth.tata.shared.common.result.AppResult

class UpdateHighContrastCommandHandler(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(command: UpdateHighContrastCommand): AppResult<PreferenceUpdate> {
        val userId = command.userId.trim()
        if (userId.isBlank()) {
            return AppResult.Failure(
                message = "The user could not be identified.",
                code = "INVALID_USER_REFERENCE",
            )
        }
        return repository.updateHighContrast(userId, command.enabled)
    }
}
