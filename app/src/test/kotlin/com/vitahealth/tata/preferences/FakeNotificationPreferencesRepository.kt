package com.vitahealth.tata.preferences

import com.vitahealth.tata.preferences.application.NotificationPreferencesRepository
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.shared.common.result.AppResult

class FakeNotificationPreferencesRepository(
    var stored: NotificationPreferences = NotificationPreferences(),
) : NotificationPreferencesRepository {
    var failWith: AppResult.Failure? = null
    val updates = mutableListOf<NotificationPreferences>()
    var getCalls = 0

    override suspend fun get(userId: String): AppResult<NotificationPreferences> {
        getCalls++
        return failWith ?: AppResult.Success(stored)
    }

    override suspend fun update(userId: String, preferences: NotificationPreferences): AppResult<NotificationPreferences> {
        updates += preferences
        failWith?.let { return it }
        stored = preferences
        return AppResult.Success(stored)
    }
}
