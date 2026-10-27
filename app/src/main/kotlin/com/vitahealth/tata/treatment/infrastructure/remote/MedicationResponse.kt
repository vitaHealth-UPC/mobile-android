package com.vitahealth.tata.treatment.infrastructure.remote

/** Medication identity, owner and registration details. */
data class MedicationResponse(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
    val active: Boolean?,
)
