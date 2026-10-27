package com.vitahealth.tata.treatment.application.queries
/** Identifies the caregiver and adult whose treatment catalog is requested. */
data class ListTreatmentsQuery(val caregiverId: String, val olderAdultId: String)
