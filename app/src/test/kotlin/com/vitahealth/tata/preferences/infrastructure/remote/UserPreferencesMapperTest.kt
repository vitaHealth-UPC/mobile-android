package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesMapperTest {
    private fun response(
        textSize: String? = "LARGE",
        quietHours: QuietHoursDto? = QuietHoursDto("22:00:00", "07:00"),
        channels: List<ChannelDto>? = listOf(ChannelDto("PUSH", true), ChannelDto("SMS", false)),
    ) =
        UserPreferencesResponse(
            userId = "user-1",
            textSize = textSize,
            highContrast = true,
            reducedMotion = false,
            readingAssistance = true,
            voiceConfirmationEnabled = false,
            quietHours = quietHours,
            notificationChannels = channels,
        )

    @Test
    fun aFullResponseIsMappedToTheDomain() {
        val preferences = response().toDomain("requested")!!

        assertEquals("user-1", preferences.userId)
        assertEquals(TextSizeLevel.LARGE, preferences.accessibility.textSize)
        assertTrue(preferences.accessibility.highContrast)
        assertFalse(preferences.accessibility.reducedMotion)
        assertTrue(preferences.accessibility.readingAssistance)
        assertFalse(preferences.accessibility.voiceConfirmation)
        assertEquals(QuietHours(22, 0, 7, 0), preferences.notifications.quietHours)
        assertEquals(ChannelType.PUSH, preferences.notifications.channels.first().type)
        assertFalse(preferences.notifications.channels[1].enabled)
    }

    @Test
    fun theRequestedUserIsUsedWhenTheResponseHasNone() {
        val preferences = response().copy(userId = null).toDomain("requested")!!

        assertEquals("requested", preferences.userId)
    }

    @Test
    fun noQuietHoursMeansNull() {
        val preferences = response(quietHours = null).toDomain("u")!!

        assertNull(preferences.notifications.quietHours)
    }

    @Test
    fun aResponseWithoutChannelsFallsBackToTheDefaultChannels() {
        val preferences = response(quietHours = null, channels = null).toDomain("u")!!

        assertTrue(preferences.notifications.channels.first { it.type == ChannelType.PUSH }.enabled)
    }

    @Test
    fun anExplicitEmptyChannelListKeepsAllChannelsDisabled() {
        val preferences = response(quietHours = null, channels = emptyList()).toDomain("u")!!
        assertTrue(preferences.notifications.channels.isEmpty())
    }

    @Test
    fun anUnknownTextSizeIsRejected() {
        assertNull(response(textSize = "HUGE").toDomain("u"))
        assertNull(response(textSize = null).toDomain("u"))
    }

    @Test
    fun anUnknownChannelIsRejected() {
        assertNull(response(channels = listOf(ChannelDto("CARRIER_PIGEON", true))).toDomain("u"))
    }

    @Test
    fun callPreferenceFromTheBackendKeepsItsEnabledState() {
        for (enabled in listOf(true, false)) {
            val preferences =
                response(channels = listOf(ChannelDto("CALL", enabled))).toDomain("u")!!
            assertEquals(ChannelType.CALL, preferences.notifications.channels.single().type)
            assertEquals(enabled, preferences.notifications.channels.single().enabled)
        }
    }

    @Test
    fun aBrokenQuietHoursValueIsRejected() {
        assertNull(response(quietHours = QuietHoursDto("late", "07:00")).toDomain("u"))
        assertNull(response(quietHours = QuietHoursDto("22:00", "22:00")).toDomain("u"))
    }

    @Test
    fun timesAreParsedWithOrWithoutSeconds() {
        assertEquals(22 to 30, parseTime("22:30"))
        assertEquals(7 to 5, parseTime("07:05:00"))
        assertNull(parseTime("25:00"))
        assertNull(parseTime("7"))
        assertNotNull(parseTime("00:00"))
    }

    @Test
    fun timesAreWrittenAsHoursAndMinutes() {
        assertEquals("07:05", formatTime(7, 5))
        assertEquals("22:00", formatTime(22, 0))
    }

    @Test
    fun policyAndContactsComeFromTheResponseWithoutInventingRecipients() {
        val value =
            response()
                .copy(
                    notificationPolicy = NotificationPolicyDto(15, 25, 90, 150, "19:30:00"),
                    emergencyContacts = listOf(EmergencyContactDto("Rosa", "+51999123456")),
                )
                .toDomain("u")!!
        assertEquals(15, value.notifications.policy!!.reminderMinutes)
        assertEquals(19, value.notifications.policy!!.dailySummaryHour)
        assertEquals(30, value.notifications.policy!!.dailySummaryMinute)
        assertEquals("Rosa", value.notifications.emergencyContacts.single().name)
        assertNull(response().toDomain("u")!!.notifications.policy)
        assertTrue(response().toDomain("u")!!.notifications.emergencyContacts.isEmpty())
    }

    @Test
    fun invalidPolicyAndDuplicateContactsAreRejected() {
        assertNull(
            response()
                .copy(notificationPolicy = NotificationPolicyDto(30, 20, 90, 150, null))
                .toDomain("u")
        )
        assertNull(
            response()
                .copy(
                    emergencyContacts =
                        listOf(
                            EmergencyContactDto("A", "+51999123456"),
                            EmergencyContactDto("B", "51999123456"),
                        )
                )
                .toDomain("u")
        )
    }
}
