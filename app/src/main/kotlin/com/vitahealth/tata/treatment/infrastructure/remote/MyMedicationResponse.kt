package com.vitahealth.tata.treatment.infrastructure.remote

/** Medication and related treatments available to the signed-in adult. */
data class MyMedicationResponse(
    val medication: MedicationResponse,
    val treatments: List<TreatmentResponse>,
)
