package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.application.UserPreferencesRemote
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteUserPreferences(
    private val api: PreferencesApiService,
) : UserPreferencesRemote {
    override suspend fun get(userId: String): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.getPreferences(userId) }

    override suspend fun updateTextSize(userId: String, textSize: TextSizeLevel): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.updateTextSize(userId, UpdateTextSizeRequest(textSize.name)) }

    override suspend fun updateHighContrast(userId: String, enabled: Boolean): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.updateHighContrast(userId, EnabledRequest(enabled)) }

    override suspend fun updateReducedMotion(userId: String, enabled: Boolean): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.updateReducedMotion(userId, EnabledRequest(enabled)) }

    override suspend fun updateVoiceConfirmation(userId: String, enabled: Boolean): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.updateVoiceConfirmation(userId, EnabledRequest(enabled)) }

    override suspend fun updateReadingAssistance(userId: String, enabled: Boolean): AppResult<UserPreferences> =
        preferencesRequest(userId) { api.updateReadingAssistance(userId, EnabledRequest(enabled)) }
}
