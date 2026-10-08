package com.vitahealth.tata.preferences.presentation.notifications

import com.vitahealth.tata.preferences.domain.model.NotificationPreferences

/** What the banner under the settings says. Each value maps to a string resource. */
enum class NotificationMessage {
    Saved,
    ErrorInvalidHours,
    ErrorRejected,
    ErrorOffline,
    ErrorUser,
    ErrorGeneric,
}

data class NotificationPreferencesUiState(
    val isLoading: Boolean = true,
    /** Null until the backend answered. */
    val preferences: NotificationPreferences? = null,
    val isSaving: Boolean = false,
    val message: NotificationMessage? = null,
) {
    val messageIsError: Boolean
        get() = message != null && message != NotificationMessage.Saved
}
