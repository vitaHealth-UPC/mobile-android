package com.vitahealth.tata.preferences.infrastructure.remote

import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
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
            emergencyContacts = emptyList(),
            notificationPolicy = NotificationPolicyDto(10, 20, 120, 180, "20:00"),
        )

    @Test
    fun missingPolicyIsRejectedInsteadOfHidingItsControls() {
        assertNull(response().copy(notificationPolicy = null).toDomain("user-1"))
    }

    @Test
    fun repeatedChannelsAreRejected() {
        assertNull(
            response(channels = listOf(ChannelDto("PUSH", true), ChannelDto("PUSH", false)))
                .toDomain("user-1")
        )
    }

    @Test
    fun malformedOrLossyTimesAreRejectedInFull() {
        for (value in
            listOf(
                "7:05",
                "07:5",
                "07:05:garbage",
                "07:05:99",
                "07:05:01",
                "07:05:00:00",
                "07:05:00.1",
                "07:05Z",
                "07:05 ",
            )) {
            assertNull(value, parseTime(value))
        }
        assertEquals(7 to 5, parseTime("07:05:00.000"))
    }

    @Test
    fun aFullResponseIsMappedToTheDomain() {
        val preferences = response().toDomain("user-1")!!

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
    fun missingOrDifferentUserIsRejectedInsteadOfBeingInferred() {
        for (id in listOf(null, "", "other", " user-1 ")) {
            assertNull(response().copy(userId = id).toDomain("user-1"))
        }
    }

    @Test
    fun noQuietHoursMeansNull() {
        val preferences = response(quietHours = null).toDomain("user-1")!!

        assertNull(preferences.notifications.quietHours)
    }

    @Test
    fun missingChannelsOrContactsAreRejectedInsteadOfInventingDefaults() {
        assertNull(response(channels = null).toDomain("user-1"))
        assertNull(response().copy(emergencyContacts = null).toDomain("user-1"))
    }

    @Test
    fun anExplicitEmptyChannelListKeepsAllChannelsDisabled() {
        val preferences = response(quietHours = null, channels = emptyList()).toDomain("user-1")!!
        assertTrue(preferences.notifications.channels.isEmpty())
    }

    @Test
    fun anUnknownTextSizeIsRejected() {
        assertNull(response(textSize = "HUGE").toDomain("user-1"))
        assertNull(response(textSize = null).toDomain("user-1"))
    }

    @Test
    fun anUnknownChannelIsRejected() {
        assertNull(
            response(channels = listOf(ChannelDto("CARRIER_PIGEON", true))).toDomain("user-1")
        )
    }

    @Test
    fun callPreferenceFromTheBackendKeepsItsEnabledState() {
        for (enabled in listOf(true, false)) {
            val preferences =
                response(channels = listOf(ChannelDto("CALL", enabled))).toDomain("user-1")!!
            assertEquals(ChannelType.CALL, preferences.notifications.channels.single().type)
            assertEquals(enabled, preferences.notifications.channels.single().enabled)
        }
    }

    @Test
    fun aBrokenQuietHoursValueIsRejected() {
        assertNull(response(quietHours = QuietHoursDto("late", "07:00")).toDomain("user-1"))
        assertNull(response(quietHours = QuietHoursDto("22:00", "22:00")).toDomain("user-1"))
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
                .toDomain("user-1")!!
        assertEquals(15, value.notifications.policy!!.reminderMinutes)
        assertEquals(19, value.notifications.policy!!.dailySummaryHour)
        assertEquals(30, value.notifications.policy!!.dailySummaryMinute)
        assertEquals("Rosa", value.notifications.emergencyContacts.single().name)
        assertNotNull(response().toDomain("user-1")!!.notifications.policy)
        assertTrue(response().toDomain("user-1")!!.notifications.emergencyContacts.isEmpty())
    }

    @Test
    fun invalidPolicyAndDuplicateContactsAreRejected() {
        assertNull(
            response()
                .copy(notificationPolicy = NotificationPolicyDto(30, 20, 90, 150, null))
                .toDomain("user-1")
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
                .toDomain("user-1")
        )
    }
}
