package com.vitahealth.tata.preferences.application

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences

/**
 * Result of changing a preference. The value is always kept on the device;
 * [syncedWithServer] is false when the backend could not be reached yet.
 */
data class PreferenceUpdate(
    val accessibility: AccessibilityPreferences,
    val syncedWithServer: Boolean,
)
