package com.vitahealth.tata.treatment.application.queries

/** Identifies the caregiver and adult whose medication catalog is requested. */
data class ListMedicationsQuery(val caregiverId: String, val olderAdultId: String)
