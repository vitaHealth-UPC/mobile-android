package com.vitahealth.tata.preferences.infrastructure.remote

/** Quiet hours and channel replacement; null quietHours removes the interval. */
data class UpdateNotificationPreferencesRequest(
    val quietHours: QuietHoursDto?,
    val channels: List<ChannelDto>,
)
