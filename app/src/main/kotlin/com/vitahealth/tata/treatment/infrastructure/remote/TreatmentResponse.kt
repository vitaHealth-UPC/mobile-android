package com.vitahealth.tata.treatment.infrastructure.remote

/** Treatment state and optional regimen configuration. */
data class TreatmentResponse(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val status: String,
    val medicationId: String?,
    val dose: String?,
    val frequency: String?,
    val scheduledTimes: List<String>?,
    val instructions: String?,
    val reminderLeadMinutes: Int?,
    val followUpDelayMinutes: Int? = null,
)
