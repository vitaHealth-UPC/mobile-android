package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NotificationReceiptValidationTest {
    private val policy = NotificationPolicy(10, 20, 120, 180, null, null)
    private val body =
        UserPreferencesResponse(
            "user",
            "MEDIUM",
            false,
            false,
            false,
            true,
            null,
            listOf(ChannelDto("PUSH", true), ChannelDto("SMS", false)),
            NotificationPolicyDto(10, 20, 120, 180, null),
            emptyList(),
        )

    private fun repository(
        response: UserPreferencesResponse
    ): RemoteNotificationPreferencesRepository {
        val api =
            Proxy.newProxyInstance(
                PreferencesApiService::class.java.classLoader,
                arrayOf(PreferencesApiService::class.java),
            ) { _, _, _ ->
                Response.success(response)
            } as PreferencesApiService
        return RemoteNotificationPreferencesRepository(api)
    }

    @Test
    fun channelsAreComparedByTypeWithoutDependingOnResponseOrder() = runBlocking {
        val requested =
            NotificationPreferences(
                channels =
                    listOf(
                        NotificationChannel(ChannelType.SMS, false),
                        NotificationChannel(ChannelType.PUSH, true),
                    )
            )
        assertTrue(repository(body).update("user", requested) is AppResult.Success)
        val changed =
            requested.copy(channels = listOf(NotificationChannel(ChannelType.PUSH, false)))
        assertEquals(
            "INVALID_PREFERENCES_RESPONSE",
            (repository(body).update("user", changed) as AppResult.Failure).code,
        )
    }

    @Test
    fun unconfirmedQuietHoursDoNotReportSuccessfulSave() = runBlocking {
        val requested =
            NotificationPreferences(
                quietHours = QuietHours.Default,
                channels =
                    listOf(
                        NotificationChannel(ChannelType.PUSH, true),
                        NotificationChannel(ChannelType.SMS, false),
                    ),
            )
        assertEquals(
            "INVALID_PREFERENCES_RESPONSE",
            (repository(body).update("user", requested) as AppResult.Failure).code,
        )
    }

    @Test
    fun policyReceiptMustConfirmTheRequestedThresholdsAndRecipients() = runBlocking {
        assertTrue(repository(body).updatePolicy("user", policy, emptyList()) is AppResult.Success)
        assertEquals(
            "INVALID_PREFERENCES_RESPONSE",
            (repository(body).updatePolicy("user", policy.copy(reminderMinutes = 15), emptyList())
                    as AppResult.Failure)
                .code,
        )
        val otherContact =
            body.copy(emergencyContacts = listOf(EmergencyContactDto("Ana", "+51999123456")))
        assertEquals(
            "INVALID_PREFERENCES_RESPONSE",
            (repository(otherContact).updatePolicy("user", policy, emptyList())
                    as AppResult.Failure)
                .code,
        )
    }
}
