package com.vitahealth.tata.preferences.infrastructure.sync

import com.vitahealth.tata.preferences.application.ports.AccessibilityLocalStore
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRemote
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.readmodels.PreferenceUpdate
import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The device copy is written first so a setting applies immediately; the backend stays the source
 * of truth and replaces the copy whenever it can be reached.
 */
class OfflineFirstUserPreferencesRepository(
    private val remote: UserPreferencesRemote,
    private val local: AccessibilityLocalStore,
) : UserPreferencesRepository {
    private val operations = Mutex()

    override fun observeAccessibility(): Flow<AccessibilityPreferences> = local.observe()

    override suspend fun sync(userId: String): AppResult<UserPreferences> =
        operations.withLock {
            if (local.isPendingSync()) {
                val pushed = pushLocalChanges(userId)
                if (pushed is AppResult.Failure) return@withLock pushed
                currentCoroutineContext().ensureActive()
                local.saveState((pushed as AppResult.Success).value.accessibility, false)
            }
            val fetched = remote.get(userId)
            currentCoroutineContext().ensureActive()
            if (fetched is AppResult.Success) local.saveState(fetched.value.accessibility, false)
            fetched
        }

    override suspend fun updateTextSize(
        userId: String,
        textSize: TextSizeLevel,
    ): AppResult<PreferenceUpdate> =
        apply(field = AccessibilityField.TEXT_SIZE, change = { it.copy(textSize = textSize) }) {
            remote.updateTextSize(userId, textSize)
        }

    override suspend fun updateHighContrast(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        apply(
            field = AccessibilityField.HIGH_CONTRAST,
            change = { it.copy(highContrast = enabled) },
        ) {
            remote.updateHighContrast(userId, enabled)
        }

    override suspend fun updateDarkTheme(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        apply(field = AccessibilityField.DARK_THEME, change = { it.copy(darkTheme = enabled) }) {
            remote.updateDarkTheme(userId, enabled)
        }

    override suspend fun updateReducedMotion(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        apply(
            field = AccessibilityField.REDUCED_MOTION,
            change = { it.copy(reducedMotion = enabled) },
        ) {
            remote.updateReducedMotion(userId, enabled)
        }

    override suspend fun updateVoiceConfirmation(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        apply(
            field = AccessibilityField.VOICE_CONFIRMATION,
            change = { it.copy(voiceConfirmation = enabled) },
        ) {
            remote.updateVoiceConfirmation(userId, enabled)
        }

    override suspend fun updateReadingAssistance(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        apply(
            field = AccessibilityField.READING_ASSISTANCE,
            change = { it.copy(readingAssistance = enabled) },
        ) {
            remote.updateReadingAssistance(userId, enabled)
        }

    private suspend fun apply(
        field: AccessibilityField,
        change: (AccessibilityPreferences) -> AccessibilityPreferences,
        send: suspend () -> AppResult<UserPreferences>,
    ): AppResult<PreferenceUpdate> =
        operations.withLock {
            val previous = local.current()
            val previouslyPending = local.pendingFields()
            val updated = change(previous)
            // Persist intent before the cancellable network operation.
            local.saveState(updated, previouslyPending + field)
            val result = send()
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success -> {
                    // A receipt for one field cannot acknowledge older unsent fields.
                    val receipt = result.value.accessibility
                    if (mergePending(receipt, updated, setOf(field)) != receipt) {
                        return@withLock AppResult.Failure(
                            "El servidor no confirmó el ajuste solicitado.",
                            code = "INVALID_RESPONSE",
                        )
                    }
                    val remaining = previouslyPending - field
                    val accepted = mergePending(receipt, updated, remaining)
                    local.saveState(accepted, remaining)
                    AppResult.Success(
                        PreferenceUpdate(accepted, syncedWithServer = remaining.isEmpty())
                    )
                }
                is AppResult.Failure ->
                    if (result.code == "NETWORK_UNAVAILABLE") {
                        AppResult.Success(PreferenceUpdate(updated, syncedWithServer = false))
                    } else {
                        if (result.code == "REQUEST_VALIDATION_FAILED") {
                            local.saveState(previous, previouslyPending)
                        }
                        result
                    }
            }
        }

    /**
     * Acknowledges each changed field independently; failed and unattempted fields stay pending.
     */
    private suspend fun pushLocalChanges(userId: String): AppResult<UserPreferences> {
        val desired = local.current()
        var remaining = local.pendingFields()
        var last: AppResult.Success<UserPreferences>? = null
        for (field in AccessibilityField.entries.filter { it in remaining }) {
            val result =
                when (field) {
                    AccessibilityField.TEXT_SIZE -> remote.updateTextSize(userId, desired.textSize)
                    AccessibilityField.HIGH_CONTRAST ->
                        remote.updateHighContrast(userId, desired.highContrast)
                    AccessibilityField.DARK_THEME ->
                        remote.updateDarkTheme(userId, desired.darkTheme)
                    AccessibilityField.REDUCED_MOTION ->
                        remote.updateReducedMotion(userId, desired.reducedMotion)
                    AccessibilityField.VOICE_CONFIRMATION ->
                        remote.updateVoiceConfirmation(userId, desired.voiceConfirmation)
                    AccessibilityField.READING_ASSISTANCE ->
                        remote.updateReadingAssistance(userId, desired.readingAssistance)
                }
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Failure -> return result
                is AppResult.Success -> {
                    if (
                        mergePending(result.value.accessibility, desired, setOf(field)) !=
                            result.value.accessibility
                    ) {
                        return AppResult.Failure(
                            "El servidor no confirmó el ajuste solicitado.",
                            code = "INVALID_RESPONSE",
                        )
                    }
                    remaining = remaining - field
                    local.saveState(
                        mergePending(result.value.accessibility, desired, remaining),
                        remaining,
                    )
                    last = result
                }
            }
        }
        return requireNotNull(last)
    }

    private fun mergePending(
        server: AccessibilityPreferences,
        local: AccessibilityPreferences,
        pending: Set<AccessibilityField>,
    ): AccessibilityPreferences =
        server.copy(
            darkTheme =
                if (AccessibilityField.DARK_THEME in pending) local.darkTheme else server.darkTheme,
            textSize =
                if (AccessibilityField.TEXT_SIZE in pending) local.textSize else server.textSize,
            highContrast =
                if (AccessibilityField.HIGH_CONTRAST in pending) local.highContrast
                else server.highContrast,
            reducedMotion =
                if (AccessibilityField.REDUCED_MOTION in pending) local.reducedMotion
                else server.reducedMotion,
            voiceConfirmation =
                if (AccessibilityField.VOICE_CONFIRMATION in pending) local.voiceConfirmation
                else server.voiceConfirmation,
            readingAssistance =
                if (AccessibilityField.READING_ASSISTANCE in pending) local.readingAssistance
                else server.readingAssistance,
        )
}
