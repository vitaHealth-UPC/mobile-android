package com.vitahealth.tata.treatment.infrastructure.remote

/** Regimen and reminder settings submitted for a treatment. */
data class ConfigureTreatmentRequest(
    val caregiverId: String,
    val medicationId: String,
    val dose: String,
    val frequency: String,
    val scheduledTimes: List<String>,
    val instructions: String,
    val reminderLeadMinutes: Int,
    val followUpDelayMinutes: Int? = null,
)
