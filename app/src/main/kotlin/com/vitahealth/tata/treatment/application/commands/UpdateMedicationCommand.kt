package com.vitahealth.tata.treatment.application.commands

/** Carries replacement medication details and the caregiver reference. */
data class UpdateMedicationCommand(
    val caregiverId: String,
    val medicationId: String,
    val name: String,
    val presentation: String,
)
