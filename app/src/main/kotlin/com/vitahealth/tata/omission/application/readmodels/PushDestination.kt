package com.vitahealth.tata.omission.application.readmodels

/** Screen a tapped omission notification opens. */
sealed interface PushDestination {
    /**
     * Caregiver alerts of the older adult. The app resolves the exact intake when supplied and
     * keeps medication matching for legacy messages without that reference.
     */
    data class CaregiverAlerts(
        val caregiverId: String,
        val olderAdultId: String,
        val medicationName: String? = null,
        val intakeId: String? = null,
    ) : PushDestination

    /** Home of the older adult, where the pending intake can be confirmed. */
    data class OlderAdultHome(val olderAdultId: String, val olderAdultName: String) :
        PushDestination

    /** Exact intake carried by a versioned reminder, independent of the next-dose selection. */
    data class OlderAdultDose(val olderAdultId: String, val intakeId: String) : PushDestination
}
