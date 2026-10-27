package com.vitahealth.tata.treatment.application.commands

/** Requests a named draft treatment for the selected adult. */
data class CreateTreatmentCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val name: String,
)
