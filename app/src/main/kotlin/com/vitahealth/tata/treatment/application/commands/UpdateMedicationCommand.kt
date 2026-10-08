package com.vitahealth.tata.treatment.application.commands

data class UpdateMedicationCommand(
    val caregiverId: String,
    val medicationId: String,
    val name: String,
    val presentation: String,
)
