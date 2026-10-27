package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours

data class UpdateNotificationPreferencesCommand(
    val userId: String,
    /** Null removes the quiet hours. */
    val quietHours: QuietHours?,
    val channels: List<NotificationChannel>,
)
