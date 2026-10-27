package com.vitahealth.tata.treatment.application.commands

/** Carries the medication, local administration times and pre-dose reminder lead for a regimen. */
data class ConfigureTreatmentCommand(
    val caregiverId: String,
    val treatmentId: String,
    val medicationId: String,
    val dose: String,
    val frequency: String,
    val scheduledTimes: List<String>,
    val instructions: String,
    val reminderLeadMinutes: Int,
    val followUpDelayMinutes: Int? = null,
)
