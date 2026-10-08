package com.vitahealth.tata.preferences.domain.model

data class NotificationPreferences(
    val quietHours: QuietHours? = null,
    val channels: List<NotificationChannel> = listOf(
        NotificationChannel(ChannelType.PUSH, enabled = true),
        NotificationChannel(ChannelType.SMS, enabled = false),
        NotificationChannel(ChannelType.EMAIL, enabled = false),
    ),
)
