package com.vitahealth.tata.monitoring.infrastructure.remote

/** `CaregiverNoteResource`; nullable for the same reason as [AlertSummaryResponse]. */
data class CaregiverNoteResponse(
    val id: Long?,
    val text: String?,
    val recordedAt: String?,
    val familiarId: String?,
)
