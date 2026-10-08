package com.vitahealth.tata.preferences.domain.model

data class UserPreferences(
    val userId: String,
    val accessibility: AccessibilityPreferences,
    val notifications: NotificationPreferences,
)
