package com.vitahealth.tata.preferences.presentation.notifications.viewmodel

import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences

/** Observable server preferences with loading, mutation and feedback state. */
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
