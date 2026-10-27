package com.vitahealth.tata.preferences.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vitahealth.tata.preferences.application.ports.AccessibilityLocalStore
import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.accessibilityDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "tata_accessibility")

/** Stores one owner partition without claiming legacy unbound device preferences. */
class DataStoreAccessibilityLocalStore
internal constructor(private val dataStore: DataStore<Preferences>, private val ownerKey: String?) :
    AccessibilityLocalStore {
    constructor(
        context: Context,
        ownerKey: String? = null,
    ) : this(context.applicationContext.accessibilityDataStore, ownerKey)

    override fun observe(): Flow<AccessibilityPreferences> = dataStore.data.map { it.toDomain() }

    override suspend fun current(): AccessibilityPreferences = observe().first()

    override suspend fun save(preferences: AccessibilityPreferences) {
        dataStore.edit {
            it[TextSizeKey] = preferences.textSize.name
            it[HighContrastKey] = preferences.highContrast
            it[ReducedMotionKey] = preferences.reducedMotion
            it[ReadingAssistanceKey] = preferences.readingAssistance
            it[VoiceConfirmationKey] = preferences.voiceConfirmation
            it[DarkThemeKey] = preferences.darkTheme
        }
    }

    override suspend fun saveState(preferences: AccessibilityPreferences, pending: Boolean) =
        saveState(preferences, if (pending) AccessibilityField.entries.toSet() else emptySet())

    override suspend fun saveState(
        preferences: AccessibilityPreferences,
        pending: Set<AccessibilityField>,
    ) {
        dataStore.edit {
            it[TextSizeKey] = preferences.textSize.name
            it[HighContrastKey] = preferences.highContrast
            it[ReducedMotionKey] = preferences.reducedMotion
            it[ReadingAssistanceKey] = preferences.readingAssistance
            it[VoiceConfirmationKey] = preferences.voiceConfirmation
            it[DarkThemeKey] = preferences.darkTheme
            it[PendingSyncKey] = pending.isNotEmpty()
            it[PendingFieldsKey] = pending.map { field -> field.name }.toSet()
        }
    }

    override suspend fun pendingFields(): Set<AccessibilityField> {
        val stored = dataStore.data.first()
        val fields = stored[PendingFieldsKey]
        return if (fields == null) {
            if (stored[PendingSyncKey] == true) AccessibilityField.entries.toSet() else emptySet()
        } else fields.map { AccessibilityField.valueOf(it) }.toSet()
    }

    override suspend fun isPendingSync(): Boolean = pendingFields().isNotEmpty()

    override suspend fun setPendingSync(pending: Boolean) {
        dataStore.edit {
            it[PendingSyncKey] = pending
            it[PendingFieldsKey] =
                if (pending) AccessibilityField.entries.map { field -> field.name }.toSet()
                else emptySet()
        }
    }

    private fun Preferences.toDomain(): AccessibilityPreferences {
        val defaults = AccessibilityPreferences.Defaults
        return AccessibilityPreferences(
            textSize =
                this[TextSizeKey]?.let { stored ->
                    runCatching { TextSizeLevel.valueOf(stored) }.getOrNull()
                } ?: defaults.textSize,
            highContrast = this[HighContrastKey] ?: defaults.highContrast,
            reducedMotion = this[ReducedMotionKey] ?: defaults.reducedMotion,
            readingAssistance = this[ReadingAssistanceKey] ?: defaults.readingAssistance,
            voiceConfirmation = this[VoiceConfirmationKey] ?: defaults.voiceConfirmation,
            darkTheme = this[DarkThemeKey] ?: defaults.darkTheme,
        )
    }

    private fun scopedKey(name: String): String = ownerKey?.let { "owner:$it/$name" } ?: name

    private val TextSizeKey = stringPreferencesKey(scopedKey("text_size"))
    private val HighContrastKey = booleanPreferencesKey(scopedKey("high_contrast"))
    private val ReducedMotionKey = booleanPreferencesKey(scopedKey("reduced_motion"))
    private val ReadingAssistanceKey = booleanPreferencesKey(scopedKey("reading_assistance"))
    private val DarkThemeKey = booleanPreferencesKey(scopedKey("dark_theme"))
    private val VoiceConfirmationKey = booleanPreferencesKey(scopedKey("voice_confirmation"))
    private val PendingFieldsKey = stringSetPreferencesKey(scopedKey("pending_fields"))
    private val PendingSyncKey = booleanPreferencesKey(scopedKey("pending_sync"))
}
