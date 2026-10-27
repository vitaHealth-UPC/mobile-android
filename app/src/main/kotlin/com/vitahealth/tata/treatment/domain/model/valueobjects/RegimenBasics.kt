package com.vitahealth.tata.treatment.domain.model.valueobjects

/** Validated dose and frequency selected together in the regimen wizard. */
data class RegimenBasics(val dosage: Dosage, val frequency: Frequency)
