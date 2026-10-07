package com.vitahealth.tata.preferences.presentation.accessibility

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences

/** What the banner under the settings says. Each value maps to a string resource. */
enum class AccessibilityMessage {
    LargeTextSaved,
    StandardTextSaved,
    HighContrastSaved,
    StandardContrastSaved,
    SavedOffline,
    ErrorRejected,
    ErrorUser,
    ErrorGeneric,
}

data class AccessibilityUiState(
    val preferences: AccessibilityPreferences = AccessibilityPreferences.Defaults,
    val isSaving: Boolean = false,
    val message: AccessibilityMessage? = null,
) {
    val messageIsError: Boolean
        get() = message == AccessibilityMessage.ErrorRejected ||
            message == AccessibilityMessage.ErrorUser ||
            message == AccessibilityMessage.ErrorGeneric
}
