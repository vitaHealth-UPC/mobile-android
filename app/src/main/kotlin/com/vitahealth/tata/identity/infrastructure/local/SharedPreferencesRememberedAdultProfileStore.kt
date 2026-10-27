package com.vitahealth.tata.identity.infrastructure.local

import android.content.Context
import com.vitahealth.tata.identity.application.ports.RememberedAdultProfileStore
import com.vitahealth.tata.identity.application.readmodels.RememberedAdultProfile
/** Encapsulates the existing device-profile keys so installed PIN shortcuts remain compatible. */
class SharedPreferencesRememberedAdultProfileStore(context: Context) : RememberedAdultProfileStore {
    private val preferences =
        context.applicationContext.getSharedPreferences("tata_adult_profile", Context.MODE_PRIVATE)

    override fun current(): RememberedAdultProfile? {
        val id = preferences.getString(ID_KEY, null)?.takeIf { it.isNotBlank() } ?: return null
        return RememberedAdultProfile(id, preferences.getString(NAME_KEY, null))
    }

    override fun remember(id: String, name: String) {
        require(id.isNotBlank()) { "An older-adult identifier is required" }
        preferences.edit().putString(ID_KEY, id).putString(NAME_KEY, name).apply()
    }

    private companion object {
        const val ID_KEY = "id"
        const val NAME_KEY = "name"
    }
}
