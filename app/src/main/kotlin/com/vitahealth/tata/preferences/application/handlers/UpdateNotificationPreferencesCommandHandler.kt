package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.application.NotificationPreferencesRepository
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommand
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.shared.common.result.AppResult

class UpdateNotificationPreferencesCommandHandler(
    private val repository: NotificationPreferencesRepository,
) {
    suspend operator fun invoke(
        command: UpdateNotificationPreferencesCommand,
    ): AppResult<NotificationPreferences> {
        val userId = command.userId.trim()
        if (userId.isBlank()) {
            return AppResult.Failure(
                message = "The user could not be identified.",
                code = "INVALID_USER_REFERENCE",
            )
        }
        if (command.channels.map { it.type }.toSet().size != command.channels.size) {
            return AppResult.Failure(
                message = "Each notification channel can appear only once.",
                code = "DUPLICATE_CHANNEL",
            )
        }
        return repository.update(
            userId,
            NotificationPreferences(quietHours = command.quietHours, channels = command.channels),
        )
    }
}
