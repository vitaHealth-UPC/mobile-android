package com.vitahealth.tata.treatment.application.commands

/** Carries the entered dose and frequency for domain validation. */
data class SetDoseFrequencyCommand(val dosage: String, val frequency: String)
