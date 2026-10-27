package com.vitahealth.tata.omission.infrastructure.local

import android.content.Context
import java.util.UUID

/** Persists only an opaque installation identity; Firebase owns storage of the push address. */
class SharedPreferencesPushInstallationStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            "tata_push_installation",
            Context.MODE_PRIVATE,
        )

    @Synchronized
    fun getOrCreate(): String {
        preferences.getString("id", null)?.let { stored ->
            if (runCatching { UUID.fromString(stored).toString() == stored }.getOrDefault(false))
                return stored
        }
        return UUID.randomUUID().toString().also { preferences.edit().putString("id", it).apply() }
    }
}
