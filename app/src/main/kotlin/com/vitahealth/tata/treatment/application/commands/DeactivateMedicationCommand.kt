package com.vitahealth.tata.treatment.application.commands

data class DeactivateMedicationCommand(
    val caregiverId: String,
    val medicationId: String,
)
