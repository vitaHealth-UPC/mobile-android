package com.vitahealth.tata.preferences.infrastructure.remote

import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies missing JSON fields remain distinguishable from explicit disabled settings. */
class PreferencesJsonBoundaryTest {
    private val json =
        """{"userId":"user-1","textSize":"MEDIUM","highContrast":false,"reducedMotion":false,"readingAssistance":false,"voiceConfirmationEnabled":false,"quietHours":null,"notificationChannels":[{"type":"PUSH","enabled":false}],"notificationPolicy":{"reminderMinutes":10,"pendingAlertMinutes":20,"omittedAlertMinutes":120,"escalationMinutes":180,"dailySummaryTime":null},"emergencyContacts":[]}"""

    private fun decode(value: String) =
        Gson().fromJson(value, UserPreferencesResponse::class.java).toDomain("user-1")

    @Test
    fun legacyReadDefaultsToLightAndExplicitDarkValueIsMapped() {
        assertFalse(decode(json)!!.accessibility.darkTheme)
        val updated =
            JsonParser.parseString(json).asJsonObject.apply { addProperty("darkTheme", true) }
        assertTrue(decode(updated.toString())!!.accessibility.darkTheme)
    }

    @Test
    fun policyAndEachThresholdMustBePresent() {
        val missing =
            JsonParser.parseString(json).asJsonObject.apply { remove("notificationPolicy") }
        assertNull(decode(missing.toString()))
        for (field in
            listOf(
                "reminderMinutes",
                "pendingAlertMinutes",
                "omittedAlertMinutes",
                "escalationMinutes",
            )) {
            val value = JsonParser.parseString(json).asJsonObject
            value.getAsJsonObject("notificationPolicy").remove(field)
            assertNull(field, decode(value.toString()))
        }
    }

    @Test
    fun explicitFalseValuesAreValidButAbsentOrNullBooleansAreNot() {
        val valid = decode(json)
        assertNotNull(valid)
        assertFalse(valid!!.accessibility.highContrast)
        assertFalse(valid.accessibility.voiceConfirmation)
        for (name in
            listOf(
                "highContrast",
                "reducedMotion",
                "readingAssistance",
                "voiceConfirmationEnabled",
            )) {
            val missing = JsonParser.parseString(json).asJsonObject.apply { remove(name) }
            assertNull(name, decode(missing.toString()))
            missing.add(name, JsonNull.INSTANCE)
            assertNull(name, decode(missing.toString()))
        }
    }

    @Test
    fun aChannelWithoutAnEnabledValueCannotSilentlyDisableNotifications() {
        val value = JsonParser.parseString(json).asJsonObject
        value.getAsJsonArray("notificationChannels")[0].asJsonObject.remove("enabled")
        assertNull(decode(value.toString()))
    }
}
