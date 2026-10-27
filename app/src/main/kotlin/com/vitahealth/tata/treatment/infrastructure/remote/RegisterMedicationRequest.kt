package com.vitahealth.tata.treatment.infrastructure.remote

/** Payload for adding a medication to an adult profile. */
data class RegisterMedicationRequest(
    val caregiverId: String,
    val name: String,
    val presentation: String,
)
