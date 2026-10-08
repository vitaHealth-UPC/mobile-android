package com.vitahealth.tata.preferences.domain.model

data class NotificationChannel(
    val type: ChannelType,
    val enabled: Boolean,
)
