package com.vitahealth.tata.identity.application.ports

import com.vitahealth.tata.identity.application.readmodels.RememberedAdultProfile
/** Stores the profile chosen on this device without storing credentials or consent decisions. */
interface RememberedAdultProfileStore {
    /** Returns the last selected profile, or null when no usable identifier is stored. */
    fun current(): RememberedAdultProfile?

    /** Remembers the profile after the care-link flow identifies the older adult. */
    fun remember(id: String, name: String)
}
