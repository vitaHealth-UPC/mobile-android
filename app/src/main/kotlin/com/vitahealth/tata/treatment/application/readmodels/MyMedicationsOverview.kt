package com.vitahealth.tata.treatment.application.readmodels

/** Medication catalog paired with the next intake available for the same adult. */
data class MyMedicationsOverview(
    val medications: List<MyMedication>,
    val nextDose: MedicationNextDose?,
)
