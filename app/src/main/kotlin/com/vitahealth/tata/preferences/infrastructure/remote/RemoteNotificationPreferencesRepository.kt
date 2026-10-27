package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.domain.repositories.NotificationPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult

/** Adapts notification settings, delivery policy and emergency recipients to HTTP. */
class RemoteNotificationPreferencesRepository(private val api: PreferencesApiService) :
    NotificationPreferencesRepository {
    override suspend fun updatePolicy(
        userId: String,
        policy: NotificationPolicy,
        contacts: List<EmergencyContact>,
    ): AppResult<NotificationPreferences> =
        preferencesRequest(userId) {
                api.updateNotificationPolicy(
                    userId,
                    UpdateNotificationPolicyRequest(
                        NotificationPolicyDto(
                            policy.reminderMinutes,
                            policy.pendingAlertMinutes,
                            policy.omittedAlertMinutes,
                            policy.escalationMinutes,
                            policy.dailySummaryHour?.let {
                                formatTime(it, requireNotNull(policy.dailySummaryMinute))
                            },
                        ),
                        contacts.map { EmergencyContactDto(it.name, it.phone) },
                    ),
                )
            }
            .notifications { received ->
                received.policy == policy && received.emergencyContacts == contacts
            }

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
            }
            .notifications { received ->
                received.quietHours == preferences.quietHours &&
                    received.channels.associate { it.type to it.enabled } ==
                        preferences.channels.associate { it.type to it.enabled }
            }

    private fun AppResult<UserPreferences>.notifications(
        accepts: (NotificationPreferences) -> Boolean = { true }
    ): AppResult<NotificationPreferences> =
        when (this) {
            is AppResult.Success ->
                if (accepts(value.notifications)) AppResult.Success(value.notifications)
                else
                    AppResult.Failure(
                        "El servidor no confirmó los ajustes solicitados.",
                        code = "INVALID_PREFERENCES_RESPONSE",
                    )
            is AppResult.Failure -> this
        }

    private fun QuietHours.toDto() =
        QuietHoursDto(
            start = formatTime(startHour, startMinute),
            end = formatTime(endHour, endMinute),
        )
}

/** The backend reads a time of day as `HH:mm`. */
internal fun formatTime(hour: Int, minute: Int): String =
    hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
