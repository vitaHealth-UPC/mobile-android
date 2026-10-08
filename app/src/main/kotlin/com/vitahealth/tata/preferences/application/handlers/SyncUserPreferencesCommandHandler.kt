package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.application.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.commands.SyncUserPreferencesCommand
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult

class SyncUserPreferencesCommandHandler(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(command: SyncUserPreferencesCommand): AppResult<UserPreferences> {
        val userId = command.userId.trim()
        if (userId.isBlank()) {
            return AppResult.Failure(
                message = "The user could not be identified.",
                code = "INVALID_USER_REFERENCE",
            )
        }
        return repository.sync(userId)
    }
}
