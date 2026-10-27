package com.vitahealth.tata.monitoring.application.queries

/** Reads the backend-authorized caregiver history for one adult. */
data class GetFamilyHistoryQuery(
    val caregiverId: String,
    val olderAdultId: String,
)
