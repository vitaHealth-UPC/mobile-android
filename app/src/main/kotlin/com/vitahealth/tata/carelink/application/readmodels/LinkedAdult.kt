package com.vitahealth.tata.carelink.application.readmodels

/** An adult profile available to the caregiver. */
data class LinkedAdult(
    val id: String,
    val name: String,
    /** Actual confirmation from the authorized Care Link listing, not inferred from registration. */
    val confirmedAt: java.time.Instant? = null,
    val careLinkId: String? = null,
)
