package com.vitahealth.tata.preferences.infrastructure.remote

/** Untrusted preference snapshot; nullable scalars distinguish absence from false. */
data class UserPreferencesResponse(
    val userId: String?,
    val textSize: String?,
    val highContrast: Boolean?,
    val reducedMotion: Boolean?,
    val readingAssistance: Boolean?,
    val voiceConfirmationEnabled: Boolean?,
    val quietHours: QuietHoursDto?,
    val notificationChannels: List<ChannelDto>?,
    val notificationPolicy: NotificationPolicyDto? = null,
    val emergencyContacts: List<EmergencyContactDto>? = null,
    val darkTheme: Boolean? = null,
)
