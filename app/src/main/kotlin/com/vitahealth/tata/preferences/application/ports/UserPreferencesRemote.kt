package com.vitahealth.tata.preferences.application.ports

import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.shared.common.result.AppResult

/** Backend contract of Accessibility & Preferences (`/api/v1/users/{userId}/preferences`). */
interface UserPreferencesRemote {
    suspend fun get(userId: String): AppResult<UserPreferences>

    suspend fun updateTextSize(userId: String, textSize: TextSizeLevel): AppResult<UserPreferences>

    suspend fun updateHighContrast(userId: String, enabled: Boolean): AppResult<UserPreferences>

    suspend fun updateDarkTheme(userId: String, enabled: Boolean): AppResult<UserPreferences>

    suspend fun updateReducedMotion(userId: String, enabled: Boolean): AppResult<UserPreferences>

    suspend fun updateVoiceConfirmation(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences>

    suspend fun updateReadingAssistance(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences>
}
