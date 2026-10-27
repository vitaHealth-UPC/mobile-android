package com.vitahealth.tata.preferences

import com.vitahealth.tata.preferences.application.ports.AccessibilityLocalStore
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRemote
import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAccessibilityLocalStore(
    initial: AccessibilityPreferences = AccessibilityPreferences.Defaults
) : AccessibilityLocalStore {
    private val state = MutableStateFlow(initial)
    private var pending = emptySet<AccessibilityField>()

    override fun observe(): Flow<AccessibilityPreferences> = state.asStateFlow()

    override suspend fun current(): AccessibilityPreferences = state.value

    override suspend fun save(preferences: AccessibilityPreferences) {
        state.value = preferences
    }

    override suspend fun saveState(preferences: AccessibilityPreferences, pending: Boolean) =
        saveState(preferences, if (pending) AccessibilityField.entries.toSet() else emptySet())

    override suspend fun saveState(
        preferences: AccessibilityPreferences,
        pending: Set<AccessibilityField>,
    ) {
        this.pending = pending
        state.value = preferences
    }

    override suspend fun pendingFields(): Set<AccessibilityField> = pending

    override suspend fun isPendingSync(): Boolean = pending.isNotEmpty()

    override suspend fun setPendingSync(pending: Boolean) {
        this.pending = if (pending) AccessibilityField.entries.toSet() else emptySet()
    }
}

/** A backend that keeps one user's preferences in memory and can be told to fail. */
class FakeUserPreferencesRemote(
    var stored: AccessibilityPreferences = AccessibilityPreferences.Defaults
) : UserPreferencesRemote {
    var failWith: AppResult.Failure? = null
    val textSizeCalls = mutableListOf<TextSizeLevel>()
    val contrastCalls = mutableListOf<Boolean>()
    val motionCalls = mutableListOf<Boolean>()
    val voiceCalls = mutableListOf<Boolean>()
    val readingCalls = mutableListOf<Boolean>()
    val darkThemeCalls = mutableListOf<Boolean>()
    var getCalls = 0

    override suspend fun get(userId: String): AppResult<UserPreferences> {
        getCalls++
        return failWith ?: AppResult.Success(snapshot(userId))
    }

    override suspend fun updateTextSize(
        userId: String,
        textSize: TextSizeLevel,
    ): AppResult<UserPreferences> {
        textSizeCalls += textSize
        failWith?.let {
            return it
        }
        stored = stored.copy(textSize = textSize)
        return AppResult.Success(snapshot(userId))
    }

    override suspend fun updateHighContrast(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences> {
        contrastCalls += enabled
        failWith?.let {
            return it
        }
        stored = stored.copy(highContrast = enabled)
        return AppResult.Success(snapshot(userId))
    }

    override suspend fun updateDarkTheme(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences> {
        darkThemeCalls += enabled
        failWith?.let {
            return it
        }
        stored = stored.copy(darkTheme = enabled)
        return AppResult.Success(snapshot(userId))
    }

    override suspend fun updateReducedMotion(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences> {
        motionCalls += enabled
        failWith?.let {
            return it
        }
        stored = stored.copy(reducedMotion = enabled)
        return AppResult.Success(snapshot(userId))
    }

    override suspend fun updateVoiceConfirmation(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences> {
        voiceCalls += enabled
        failWith?.let {
            return it
        }
        stored = stored.copy(voiceConfirmation = enabled)
        return AppResult.Success(snapshot(userId))
    }

    override suspend fun updateReadingAssistance(
        userId: String,
        enabled: Boolean,
    ): AppResult<UserPreferences> {
        readingCalls += enabled
        failWith?.let {
            return it
        }
        stored = stored.copy(readingAssistance = enabled)
        return AppResult.Success(snapshot(userId))
    }

    private fun snapshot(userId: String) =
        UserPreferences(userId, stored, NotificationPreferences())
}

val NetworkDown = AppResult.Failure(message = "offline", code = "NETWORK_UNAVAILABLE")
val RejectedByServer = AppResult.Failure(message = "rejected", code = "REQUEST_VALIDATION_FAILED")
