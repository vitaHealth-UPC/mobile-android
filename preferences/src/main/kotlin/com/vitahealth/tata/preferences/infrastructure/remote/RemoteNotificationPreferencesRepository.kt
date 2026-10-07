package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.application.NotificationPreferencesRepository
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.shared.common.result.AppResult

class RemoteNotificationPreferencesRepository(
    private val api: PreferencesApiService,
) : NotificationPreferencesRepository {
    override suspend fun get(userId: String): AppResult<NotificationPreferences> =
        preferencesRequest(userId) { api.getPreferences(userId) }.notifications()

    override suspend fun update(
        userId: String,
        preferences: NotificationPreferences,
    ): AppResult<NotificationPreferences> =
        preferencesRequest(userId) {
            api.updateNotificationPreferences(
                userId,
                UpdateNotificationPreferencesRequest(
                    quietHours = preferences.quietHours?.toDto(),
                    channels = preferences.channels.map { ChannelDto(it.type.name, it.enabled) },
                ),
            )
        }.notifications()

    private fun AppResult<com.vitahealth.tata.preferences.domain.model.UserPreferences>.notifications():
        AppResult<NotificationPreferences> =
        when (this) {
            is AppResult.Success -> AppResult.Success(value.notifications)
            is AppResult.Failure -> this
        }

    private fun QuietHours.toDto() = QuietHoursDto(
        start = formatTime(startHour, startMinute),
        end = formatTime(endHour, endMinute),
    )
}

/** The backend reads a time of day as `HH:mm`. */
internal fun formatTime(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
