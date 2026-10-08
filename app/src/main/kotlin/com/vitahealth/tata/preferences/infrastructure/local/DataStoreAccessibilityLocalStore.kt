package com.vitahealth.tata.preferences.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vitahealth.tata.preferences.application.AccessibilityLocalStore
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.accessibilityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tata_accessibility",
)

class DataStoreAccessibilityLocalStore(
    context: Context,
) : AccessibilityLocalStore {
    private val dataStore = context.applicationContext.accessibilityDataStore

    override fun observe(): Flow<AccessibilityPreferences> = dataStore.data.map { it.toDomain() }

    override suspend fun current(): AccessibilityPreferences = observe().first()

    override suspend fun save(preferences: AccessibilityPreferences) {
        dataStore.edit {
            it[TextSizeKey] = preferences.textSize.name
            it[HighContrastKey] = preferences.highContrast
            it[ReducedMotionKey] = preferences.reducedMotion
            it[ReadingAssistanceKey] = preferences.readingAssistance
            it[VoiceConfirmationKey] = preferences.voiceConfirmation
        }
    }

    override suspend fun isPendingSync(): Boolean = dataStore.data.first()[PendingSyncKey] ?: false

    override suspend fun setPendingSync(pending: Boolean) {
        dataStore.edit { it[PendingSyncKey] = pending }
    }

    private fun Preferences.toDomain(): AccessibilityPreferences {
        val defaults = AccessibilityPreferences.Defaults
        return AccessibilityPreferences(
            textSize = this[TextSizeKey]
                ?.let { stored -> runCatching { TextSizeLevel.valueOf(stored) }.getOrNull() }
                ?: defaults.textSize,
            highContrast = this[HighContrastKey] ?: defaults.highContrast,
            reducedMotion = this[ReducedMotionKey] ?: defaults.reducedMotion,
            readingAssistance = this[ReadingAssistanceKey] ?: defaults.readingAssistance,
            voiceConfirmation = this[VoiceConfirmationKey] ?: defaults.voiceConfirmation,
        )
    }

    private companion object {
        val TextSizeKey = stringPreferencesKey("text_size")
        val HighContrastKey = booleanPreferencesKey("high_contrast")
        val ReducedMotionKey = booleanPreferencesKey("reduced_motion")
        val ReadingAssistanceKey = booleanPreferencesKey("reading_assistance")
        val VoiceConfirmationKey = booleanPreferencesKey("voice_confirmation")
        val PendingSyncKey = booleanPreferencesKey("pending_sync")
    }
}
