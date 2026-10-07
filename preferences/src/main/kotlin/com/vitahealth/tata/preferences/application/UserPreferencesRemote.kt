package com.vitahealth.tata.preferences.application

import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult

/** Backend contract of Accessibility & Preferences (`/api/v1/users/{userId}/preferences`). */
interface UserPreferencesRemote {
    suspend fun get(userId: String): AppResult<UserPreferences>

    suspend fun updateTextSize(userId: String, textSize: TextSizeLevel): AppResult<UserPreferences>

    suspend fun updateHighContrast(userId: String, enabled: Boolean): AppResult<UserPreferences>
}
