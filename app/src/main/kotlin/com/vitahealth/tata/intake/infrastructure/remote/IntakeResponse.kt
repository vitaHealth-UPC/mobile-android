package com.vitahealth.tata.intake.infrastructure.remote

/** Transport representation of a scheduled intake and its server-owned outcome. */
data class IntakeResponse(
    val id: String,
    val treatmentId: String,
    val medicationId: String,
    val olderAdultId: String,
    val medicationName: String,
    val dose: String,
    val instructions: String?,
    val scheduledAt: String,
    val status: String,
    val confirmedAt: String? = null,
    val confirmationChannel: String? = null,
    val alreadyConfirmed: Boolean = false,
)
