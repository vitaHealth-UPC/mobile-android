package com.vitahealth.tata.omission.infrastructure.local

import android.content.Context
import com.vitahealth.tata.omission.application.policies.OmissionPushTopics
import com.vitahealth.tata.omission.application.ports.LegacyPushTopicSource
import com.vitahealth.tata.omission.application.ports.PushTargetStore

/** Only identifiers and the display name the screens already show; no health data is stored. */
class SharedPreferencesPushTargetStore(context: Context) : PushTargetStore, LegacyPushTopicSource {
    private val preferences =
        context.applicationContext.getSharedPreferences("tata_push_targets", Context.MODE_PRIVATE)

    override fun rememberCaregiver(olderAdultId: String, caregiverId: String) {
        preferences.edit().putString(CAREGIVER + olderAdultId, caregiverId).apply()
    }

    override fun rememberOlderAdult(olderAdultId: String, olderAdultName: String) {
        preferences.edit().putString(OLDER_ADULT + olderAdultId, olderAdultName).apply()
    }

    override fun caregiverFor(olderAdultId: String): String? =
        preferences.getString(CAREGIVER + olderAdultId, null)

    override fun olderAdultName(olderAdultId: String): String? =
        preferences.getString(OLDER_ADULT + olderAdultId, null)

    /** Older versions persisted each target before asking Firebase to subscribe. */
    override fun topics(): Set<String> =
        preferences.all.keys
            .mapNotNull { key ->
                when {
                    key.startsWith(CAREGIVER) ->
                        key.removePrefix(CAREGIVER)
                            .takeIf(String::isNotBlank)
                            ?.let(OmissionPushTopics::forCaregiversOf)
                    key.startsWith(OLDER_ADULT) ->
                        key.removePrefix(OLDER_ADULT)
                            .takeIf(String::isNotBlank)
                            ?.let(OmissionPushTopics::forOlderAdult)
                    else -> null
                }
            }
            .toSet()

    private companion object {
        const val CAREGIVER = "caregiver_of_"
        const val OLDER_ADULT = "older_adult_"
    }
}
