package com.vitahealth.tata.omission

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.omission.infrastructure.local.SharedPreferencesPushTargetStore
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class LegacyPushTargetsMigrationTest {
    @Test
    fun discoversBothHistoricalAudiencesWithoutDeletingNavigationTargets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("tata_push_targets", Context.MODE_PRIVATE)
        val adult = UUID.randomUUID().toString()
        val otherAdult = UUID.randomUUID().toString()
        val caregiverKey = "caregiver_of_$adult"
        val adultKey = "older_adult_$otherAdult"
        try {
            // Seed the persisted format of the old app, not the current write methods.
            assertTrue(
                preferences
                    .edit()
                    .putString(caregiverKey, "previous-caregiver")
                    .putString(adultKey, "Previous adult")
                    .commit()
            )
            val store = SharedPreferencesPushTargetStore(context)
            val topics = store.topics()
            assertTrue(topics.contains("caregivers-of-$adult"))
            assertTrue(topics.contains("user-$otherAdult"))
            assertEquals("previous-caregiver", store.caregiverFor(adult))
            assertEquals("Previous adult", store.olderAdultName(otherAdult))
            // Recreating the adapter/process preserves the retirement inventory for retry.
            assertTrue(SharedPreferencesPushTargetStore(context).topics().containsAll(topics))
        } finally {
            preferences.edit().remove(caregiverKey).remove(adultKey).commit()
        }
    }
}
