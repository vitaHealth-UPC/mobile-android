package com.vitahealth.tata.preferences.infrastructure

import com.vitahealth.tata.preferences.application.AccessibilityLocalStore
import com.vitahealth.tata.preferences.application.PreferenceUpdate
import com.vitahealth.tata.preferences.application.UserPreferencesRemote
import com.vitahealth.tata.preferences.application.UserPreferencesRepository
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * The device copy is written first so a setting applies immediately; the backend stays the
 * source of truth and replaces the copy whenever it can be reached.
 */
class OfflineFirstUserPreferencesRepository(
    private val remote: UserPreferencesRemote,
    private val local: AccessibilityLocalStore,
) : UserPreferencesRepository {
    override fun observeAccessibility(): Flow<AccessibilityPreferences> = local.observe()

    override suspend fun sync(userId: String): AppResult<UserPreferences> {
        if (local.isPendingSync()) {
            val pushed = pushLocalChanges(userId)
            if (pushed is AppResult.Failure) return pushed
            local.setPendingSync(false)
        }
        val fetched = remote.get(userId)
        if (fetched is AppResult.Success) local.save(fetched.value.accessibility)
        return fetched
    }

    override suspend fun updateTextSize(
        userId: String,
        textSize: TextSizeLevel,
    ): AppResult<PreferenceUpdate> = apply(change = { it.copy(textSize = textSize) }) {
        remote.updateTextSize(userId, textSize)
    }

    override suspend fun updateHighContrast(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = apply(change = { it.copy(highContrast = enabled) }) {
        remote.updateHighContrast(userId, enabled)
    }

    override suspend fun updateReducedMotion(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = apply(change = { it.copy(reducedMotion = enabled) }) {
        remote.updateReducedMotion(userId, enabled)
    }

    override suspend fun updateVoiceConfirmation(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = apply(change = { it.copy(voiceConfirmation = enabled) }) {
        remote.updateVoiceConfirmation(userId, enabled)
    }

    override suspend fun updateReadingAssistance(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = apply(change = { it.copy(readingAssistance = enabled) }) {
        remote.updateReadingAssistance(userId, enabled)
    }

    private suspend fun apply(
        change: (AccessibilityPreferences) -> AccessibilityPreferences,
        send: suspend () -> AppResult<UserPreferences>,
    ): AppResult<PreferenceUpdate> {
        val previous = local.current()
        val updated = change(previous)
        local.save(updated)

        return when (val result = send()) {
            is AppResult.Success -> {
                local.save(result.value.accessibility)
                AppResult.Success(PreferenceUpdate(result.value.accessibility, syncedWithServer = true))
            }

            is AppResult.Failure -> if (result.code == "NETWORK_UNAVAILABLE") {
                local.setPendingSync(true)
                AppResult.Success(PreferenceUpdate(updated, syncedWithServer = false))
            } else {
                // The backend rejected the change, so the device must not keep it either.
                local.save(previous)
                result
            }
        }
    }

    /** Every preference that can be changed offline must be sent here. */
    private suspend fun pushLocalChanges(userId: String): AppResult<UserPreferences> {
        val current = local.current()
        val textSize = remote.updateTextSize(userId, current.textSize)
        if (textSize is AppResult.Failure) return textSize
        val contrast = remote.updateHighContrast(userId, current.highContrast)
        if (contrast is AppResult.Failure) return contrast
        val motion = remote.updateReducedMotion(userId, current.reducedMotion)
        if (motion is AppResult.Failure) return motion
        val voice = remote.updateVoiceConfirmation(userId, current.voiceConfirmation)
        if (voice is AppResult.Failure) return voice
        return remote.updateReadingAssistance(userId, current.readingAssistance)
    }
}
