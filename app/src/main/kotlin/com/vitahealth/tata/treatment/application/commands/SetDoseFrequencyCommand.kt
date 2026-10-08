package com.vitahealth.tata.treatment.application.commands

data class SetDoseFrequencyCommand(
    val dosage: String,
    val frequency: String,
)
