package com.vitahealth.tata.preferences.application

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeAccessibility(): Flow<AccessibilityPreferences>

    /** Sends changes that could not reach the backend, then replaces the local copy with the server's. */
    suspend fun sync(userId: String): AppResult<UserPreferences>

    suspend fun updateTextSize(userId: String, textSize: TextSizeLevel): AppResult<PreferenceUpdate>

    suspend fun updateHighContrast(userId: String, enabled: Boolean): AppResult<PreferenceUpdate>

    suspend fun updateReducedMotion(userId: String, enabled: Boolean): AppResult<PreferenceUpdate>

    suspend fun updateVoiceConfirmation(userId: String, enabled: Boolean): AppResult<PreferenceUpdate>
}
