package com.vitahealth.tata.preferences.domain.model

/** Delays from the scheduled intake. They control notices, never treatment dosage. */
data class NotificationPolicy(
    val reminderMinutes: Int,
    val pendingAlertMinutes: Int,
    val omittedAlertMinutes: Int,
    val escalationMinutes: Int,
    val dailySummaryHour: Int?,
    val dailySummaryMinute: Int?,
) {
    init {
        require(reminderMinutes >= 1 && escalationMinutes <= 1440)
        require(
            reminderMinutes < pendingAlertMinutes &&
                pendingAlertMinutes < omittedAlertMinutes &&
                omittedAlertMinutes < escalationMinutes
        )
        require((dailySummaryHour == null) == (dailySummaryMinute == null))
        require(dailySummaryHour == null || dailySummaryHour in 0..23)
        require(dailySummaryMinute == null || dailySummaryMinute in 0..59)
    }
}
