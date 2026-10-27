package com.vitahealth.tata.preferences.presentation.accessibility.viewmodel

import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage

/** What the banner under the settings says. Each value maps to a string resource. */
enum class AccessibilityMessage {
    LargeTextSaved,
    StandardTextSaved,
    HighContrastSaved,
    DarkThemeSaved,
    LightThemeSaved,
    StandardContrastSaved,
    ReducedMotionSaved,
    StandardMotionSaved,
    VoiceConfirmationSaved,
    VoiceConfirmationOffSaved,
    ReadingAssistanceSaved,
    StandardReadingSaved,
    SavedOffline,
    ErrorRejected,
    ErrorUser,
    ErrorGeneric,
}

data class AccessibilityUiState(
    val preferences: AccessibilityPreferences = AccessibilityPreferences.Defaults,
    val isSaving: Boolean = false,
    val message: AccessibilityMessage? = null,
    val appLanguage: AppLanguage = AppLanguage.SPANISH,
    val isLanguageSaving: Boolean = false,
    val languageError: String? = null,
) {
    val messageIsError: Boolean
        get() =
            message == AccessibilityMessage.ErrorRejected ||
                message == AccessibilityMessage.ErrorUser ||
                message == AccessibilityMessage.ErrorGeneric
}
