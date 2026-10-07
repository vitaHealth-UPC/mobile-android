package com.vitahealth.tata.treatment.application.commands

data class RegisterMedicationCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
)
