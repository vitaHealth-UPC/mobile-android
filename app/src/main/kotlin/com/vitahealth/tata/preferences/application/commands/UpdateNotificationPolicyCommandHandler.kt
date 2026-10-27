package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.repositories.NotificationPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult

class UpdateNotificationPolicyCommandHandler(
    private val repository: NotificationPreferencesRepository
) {
    suspend operator fun invoke(
        command: UpdateNotificationPolicyCommand
    ): AppResult<NotificationPreferences> {
        if (command.userId.isBlank())
            return AppResult.Failure("User is required.", code = "INVALID_USER_REFERENCE")
        if (
            command.contacts.size > 5 ||
                command.contacts.map { it.phone.removePrefix("+") }.distinct().size !=
                    command.contacts.size
        ) {
            return AppResult.Failure(
                "Contacts must have distinct phones.",
                code = "REQUEST_VALIDATION_FAILED",
            )
        }
        return repository.updatePolicy(
            command.userId.trim(),
            command.policy,
            command.contacts.toList(),
        )
    }
}
