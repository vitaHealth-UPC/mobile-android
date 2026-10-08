package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.application.NotificationPreferencesRepository
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQuery
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.shared.common.result.AppResult

class GetNotificationPreferencesQueryHandler(
    private val repository: NotificationPreferencesRepository,
) {
    suspend operator fun invoke(query: GetNotificationPreferencesQuery): AppResult<NotificationPreferences> {
        val userId = query.userId.trim()
        if (userId.isBlank()) {
            return AppResult.Failure(
                message = "The user could not be identified.",
                code = "INVALID_USER_REFERENCE",
            )
        }
        return repository.get(userId)
    }
}
