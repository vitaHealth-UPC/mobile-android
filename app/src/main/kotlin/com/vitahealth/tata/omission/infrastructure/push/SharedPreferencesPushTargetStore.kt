package com.vitahealth.tata.omission.infrastructure.push

import android.content.Context
import com.vitahealth.tata.omission.application.PushTargetStore

/** Only identifiers and the display name the screens already show; no health data is stored. */
class SharedPreferencesPushTargetStore(context: Context) : PushTargetStore {
    private val preferences = context.applicationContext.getSharedPreferences("tata_push_targets", Context.MODE_PRIVATE)

    override fun rememberCaregiver(olderAdultId: String, caregiverId: String) {
        preferences.edit().putString(CAREGIVER + olderAdultId, caregiverId).apply()
    }

    override fun rememberOlderAdult(olderAdultId: String, olderAdultName: String) {
        preferences.edit().putString(OLDER_ADULT + olderAdultId, olderAdultName).apply()
    }

    override fun caregiverFor(olderAdultId: String): String? = preferences.getString(CAREGIVER + olderAdultId, null)

    override fun olderAdultName(olderAdultId: String): String? = preferences.getString(OLDER_ADULT + olderAdultId, null)

    private companion object {
        const val CAREGIVER = "caregiver_of_"
        const val OLDER_ADULT = "older_adult_"
    }
}
