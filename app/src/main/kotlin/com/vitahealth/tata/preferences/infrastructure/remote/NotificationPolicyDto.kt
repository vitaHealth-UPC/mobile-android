package com.vitahealth.tata.preferences.infrastructure.remote

/** Delivery thresholds and optional daily summary time. */
data class NotificationPolicyDto(
    val reminderMinutes: Int,
    val pendingAlertMinutes: Int,
    val omittedAlertMinutes: Int,
    val escalationMinutes: Int,
    val dailySummaryTime: String?,
)
