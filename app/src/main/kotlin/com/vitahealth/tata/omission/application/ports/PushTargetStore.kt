package com.vitahealth.tata.omission.application.ports

/** Who this device follows, so a tapped notification can open the right screen. */
interface PushTargetStore {
    fun rememberCaregiver(olderAdultId: String, caregiverId: String)

    fun rememberOlderAdult(olderAdultId: String, olderAdultName: String)

    fun caregiverFor(olderAdultId: String): String?

    fun olderAdultName(olderAdultId: String): String?
}
