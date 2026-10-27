package com.vitahealth.tata.treatment.application.commands

/** Carries the identifying medication data entered for an adult. */
data class RegisterMedicationCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
)
