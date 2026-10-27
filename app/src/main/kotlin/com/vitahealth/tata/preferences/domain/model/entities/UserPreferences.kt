package com.vitahealth.tata.preferences.domain.model.entities

import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences

/** Configuration belonging to one user and grouping accessibility and delivery preferences. */
data class UserPreferences(
    val userId: String,
    val accessibility: AccessibilityPreferences,
    val notifications: NotificationPreferences,
) {
    init {
        require(userId.isNotBlank()) { "preferences must belong to one user" }
    }
}
