package com.vitahealth.tata.preferences.domain.model.valueobjects

/** Delivery channel selection and whether that channel is enabled. */
data class NotificationChannel(val type: ChannelType, val enabled: Boolean)
