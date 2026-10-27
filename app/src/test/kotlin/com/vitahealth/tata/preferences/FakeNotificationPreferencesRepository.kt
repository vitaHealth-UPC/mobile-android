package com.vitahealth.tata.preferences

import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.repositories.NotificationPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult

class FakeNotificationPreferencesRepository(
    var stored: NotificationPreferences = NotificationPreferences()
) : NotificationPreferencesRepository {
    var failWith: AppResult.Failure? = null
    val updates = mutableListOf<NotificationPreferences>()
    var getCalls = 0

    override suspend fun updatePolicy(
        userId: String,
        policy: com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy,
        contacts: List<com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact>,
    ): AppResult<NotificationPreferences> {
        failWith?.let {
            return it
        }
        stored = stored.copy(policy = policy, emergencyContacts = contacts)
        updates += stored
        return AppResult.Success(stored)
    }

    override suspend fun get(userId: String): AppResult<NotificationPreferences> {
        getCalls++
        return failWith ?: AppResult.Success(stored)
    }

    override suspend fun update(
        userId: String,
        preferences: NotificationPreferences,
    ): AppResult<NotificationPreferences> {
        updates += preferences
        failWith?.let {
            return it
        }
        stored = stored.copy(quietHours = preferences.quietHours, channels = preferences.channels)
        return AppResult.Success(stored)
    }
}
