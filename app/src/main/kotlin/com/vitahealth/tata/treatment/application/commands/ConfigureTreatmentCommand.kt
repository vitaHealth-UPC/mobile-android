package com.vitahealth.tata.treatment.application.commands

data class ConfigureTreatmentCommand(
    val caregiverId: String,
    val treatmentId: String,
    val medicationId: String,
    val dose: String,
    val frequency: String,
    val scheduledTimes: List<String>,
    val instructions: String,
    val reminderLeadMinutes: Int,
)
