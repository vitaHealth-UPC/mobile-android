package com.vitahealth.tata.treatment.infrastructure.remote

/** Updated medication details submitted by the caregiver. */
data class UpdateMedicationRequest(
    val caregiverId: String,
    val name: String,
    val presentation: String,
)
