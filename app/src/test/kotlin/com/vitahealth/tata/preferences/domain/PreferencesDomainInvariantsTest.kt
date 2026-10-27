package com.vitahealth.tata.preferences.domain

import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Checks identity and scheduling invariants without Android storage. */
class PreferencesDomainInvariantsTest {

    @Test
    fun preferencesRequireAnOwnerEvenAfterCopy() {
        val preference = UserPreferences(
            userId = "user-1",
            accessibility = AccessibilityPreferences.Defaults,
            notifications = NotificationPreferences(),
        )

        assertThrows(IllegalArgumentException::class.java) {
            preference.copy(userId = " ")
        }
        assertEquals("user-1", preference.userId)
    }

    @Test
    fun quietHoursMayCrossMidnightButNotCollapseToOneInstant() {
        val quiet = QuietHours(22, 0, 7, 0)
        assertEquals(22, quiet.startHour)
        assertThrows(IllegalArgumentException::class.java) {
            quiet.copy(endHour = 22, endMinute = 0)
        }
    }

    @Test
    fun escalationMustFollowOmissionAndSummaryTimeMustBeComplete() {
        val policy = NotificationPolicy(5, 10, 20, 30, null, null)
        assertThrows(IllegalArgumentException::class.java) {
            policy.copy(escalationMinutes = 15)
        }
        assertThrows(IllegalArgumentException::class.java) {
            policy.copy(dailySummaryHour = 12)
        }
    }
}
