package com.vitahealth.tata.carelink.infrastructure.remote

/** Confirmed relationship returned when listing a caregiver's linked adults. */
data class ConfirmedLinkResponse(
    val id: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val confirmedAt: String? = null,
)
