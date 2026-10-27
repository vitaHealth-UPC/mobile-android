package com.vitahealth.tata.preferences.domain.model.valueobjects

/** Delivery configuration combining quiet hours, recipients, policy and channel selection. */
data class NotificationPreferences(
    val quietHours: QuietHours? = null,
    /** Null when connected to a service without the notification policy contract. */
    val policy: NotificationPolicy? = null,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val channels: List<NotificationChannel> =
        listOf(
            NotificationChannel(ChannelType.PUSH, enabled = true),
            NotificationChannel(ChannelType.SMS, enabled = false),
            NotificationChannel(ChannelType.EMAIL, enabled = false),
            NotificationChannel(ChannelType.CALL, enabled = false),
        ),
)
