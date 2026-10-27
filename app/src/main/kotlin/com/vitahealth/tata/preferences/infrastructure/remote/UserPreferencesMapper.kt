package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel

/** Returns null when the response carries a value this app does not know. */
internal fun UserPreferencesResponse.toDomain(requestedUserId: String): UserPreferences? {
    if (requestedUserId.isBlank() || userId != requestedUserId) return null
    val contrast = highContrast ?: return null
    val motion = reducedMotion ?: return null
    val reading = readingAssistance ?: return null
    val voice = voiceConfirmationEnabled ?: return null
    val textSize =
        runCatching { TextSizeLevel.valueOf(textSize.orEmpty()) }.getOrNull() ?: return null
    val channels =
        (notificationChannels ?: return null).map { dto ->
            val type = runCatching { ChannelType.valueOf(dto.type) }.getOrNull() ?: return null
            NotificationChannel(type = type, enabled = dto.enabled ?: return null)
        }
    if (channels.map { it.type }.distinct().size != channels.size) return null
    val quietHours =
        quietHours?.let { dto ->
            val start = parseTime(dto.start) ?: return null
            val end = parseTime(dto.end) ?: return null
            runCatching { QuietHours(start.first, start.second, end.first, end.second) }.getOrNull()
                ?: return null
        }

    val policy =
        (notificationPolicy ?: return null).let { dto ->
            val time = dto.dailySummaryTime?.let { parseTime(it) ?: return null }
            runCatching {
                    NotificationPolicy(
                        dto.reminderMinutes,
                        dto.pendingAlertMinutes,
                        dto.omittedAlertMinutes,
                        dto.escalationMinutes,
                        time?.first,
                        time?.second,
                    )
                }
                .getOrNull() ?: return null
        }
    val contacts =
        (emergencyContacts ?: return null).map { dto ->
            runCatching { EmergencyContact.fromInput(dto.name, dto.phone) }.getOrNull()
                ?: return null
        }
    if (
        contacts.size > 5 ||
            contacts.map { it.phone.removePrefix("+") }.distinct().size != contacts.size
    )
        return null
    return UserPreferences(
        userId = requestedUserId,
        accessibility =
            AccessibilityPreferences(
                textSize = textSize,
                highContrast = contrast,
                darkTheme = darkTheme ?: false,
                reducedMotion = motion,
                readingAssistance = reading,
                voiceConfirmation = voice,
            ),
        notifications =
            NotificationPreferences(
                quietHours = quietHours,
                channels = channels,
                policy = policy,
                emergencyContacts = contacts,
            ),
    )
}

/** Accepts backend minute-precision times without silently discarding nonzero seconds. */
internal fun parseTime(value: String): Pair<Int, Int>? {
    val match = minutePrecisionTime.matchEntire(value) ?: return null
    return match.groupValues[1].toInt() to match.groupValues[2].toInt()
}

private val minutePrecisionTime = Regex("""([01][0-9]|2[0-3]):([0-5][0-9])(?::00(?:\.0{1,9})?)?""")
