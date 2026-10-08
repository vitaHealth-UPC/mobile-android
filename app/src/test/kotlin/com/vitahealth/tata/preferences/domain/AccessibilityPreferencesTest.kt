package com.vitahealth.tata.preferences.domain

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AccessibilityPreferencesTest {
    @Test
    fun defaultsMatchTheBackendDefaults() {
        val defaults = AccessibilityPreferences.Defaults

        assertEquals(TextSizeLevel.MEDIUM, defaults.textSize)
        assertFalse(defaults.highContrast)
        assertFalse(defaults.reducedMotion)
        assertFalse(defaults.readingAssistance)
        assertTrue(defaults.voiceConfirmation)
    }

    @Test
    fun largeTextCountsFromLargeUp() {
        assertFalse(AccessibilityPreferences(textSize = TextSizeLevel.SMALL).largeTextEnabled)
        assertFalse(AccessibilityPreferences(textSize = TextSizeLevel.MEDIUM).largeTextEnabled)
        assertTrue(AccessibilityPreferences(textSize = TextSizeLevel.LARGE).largeTextEnabled)
        assertTrue(AccessibilityPreferences(textSize = TextSizeLevel.EXTRA_LARGE).largeTextEnabled)
    }

    @Test
    fun scaleFactorGrowsWithTheSize() {
        val factors = TextSizeLevel.entries.map { it.scaleFactor }

        assertEquals(factors.sorted(), factors)
        assertEquals(1.0f, TextSizeLevel.MEDIUM.scaleFactor, 0.0001f)
    }

    @Test
    fun quietHoursMayCrossMidnight() {
        val hours = QuietHours(startHour = 22, startMinute = 0, endHour = 7, endMinute = 0)

        assertEquals(22, hours.startHour)
        assertEquals(7, hours.endHour)
    }

    @Test
    fun quietHoursRejectOutOfRangeValues() {
        assertThrows(IllegalArgumentException::class.java) { QuietHours(24, 0, 7, 0) }
        assertThrows(IllegalArgumentException::class.java) { QuietHours(22, 60, 7, 0) }
    }

    @Test
    fun quietHoursRejectAnEmptyInterval() {
        assertThrows(IllegalArgumentException::class.java) { QuietHours(22, 0, 22, 0) }
    }
}
