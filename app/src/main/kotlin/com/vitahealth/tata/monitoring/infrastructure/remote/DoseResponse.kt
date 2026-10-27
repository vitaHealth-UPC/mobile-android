package com.vitahealth.tata.monitoring.infrastructure.remote

/** Scheduled intake resource used by the family agenda and next dose. */
data class DoseResponse(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String,
    val scheduledAt: String,
    val status: String,
    val olderAdultId: String? = null,
)
