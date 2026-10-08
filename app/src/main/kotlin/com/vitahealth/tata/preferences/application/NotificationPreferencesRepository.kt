package com.vitahealth.tata.preferences.application

import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.shared.common.result.AppResult

/** Quiet hours and channels. They are used by the backend when it sends notices, so there is no device copy. */
interface NotificationPreferencesRepository {
    suspend fun get(userId: String): AppResult<NotificationPreferences>

    /** The backend replaces quiet hours and channels together. */
    suspend fun update(userId: String, preferences: NotificationPreferences): AppResult<NotificationPreferences>
}
