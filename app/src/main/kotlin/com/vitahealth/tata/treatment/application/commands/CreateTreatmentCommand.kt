package com.vitahealth.tata.treatment.application.commands

data class CreateTreatmentCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val name: String,
)
