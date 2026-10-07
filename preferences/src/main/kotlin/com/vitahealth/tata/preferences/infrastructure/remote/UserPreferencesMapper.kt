package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.domain.model.UserPreferences

/** Returns null when the response carries a value this app does not know. */
internal fun UserPreferencesResponse.toDomain(requestedUserId: String): UserPreferences? {
    val textSize = runCatching { TextSizeLevel.valueOf(textSize.orEmpty()) }.getOrNull() ?: return null
    val channels = notificationChannels.orEmpty().map { dto ->
        val type = runCatching { ChannelType.valueOf(dto.type) }.getOrNull() ?: return null
        NotificationChannel(type = type, enabled = dto.enabled)
    }
    val quietHours = quietHours?.let { dto ->
        val start = parseTime(dto.start) ?: return null
        val end = parseTime(dto.end) ?: return null
        runCatching { QuietHours(start.first, start.second, end.first, end.second) }.getOrNull() ?: return null
    }

    return UserPreferences(
        userId = userId?.takeIf { it.isNotBlank() } ?: requestedUserId,
        accessibility = AccessibilityPreferences(
            textSize = textSize,
            highContrast = highContrast,
            reducedMotion = reducedMotion,
            readingAssistance = readingAssistance,
            voiceConfirmation = voiceConfirmationEnabled,
        ),
        notifications = if (channels.isEmpty() && quietHours == null) {
            NotificationPreferences()
        } else {
            NotificationPreferences(quietHours = quietHours, channels = channels)
        },
    )
}

/** Accepts `HH:mm` and `HH:mm:ss`, the two shapes Jackson uses for a time of day. */
internal fun parseTime(value: String): Pair<Int, Int>? {
    val parts = value.split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return if (hour in 0..23 && minute in 0..59) hour to minute else null
}
