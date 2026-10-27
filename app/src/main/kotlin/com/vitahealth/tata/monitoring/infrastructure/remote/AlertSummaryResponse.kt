package com.vitahealth.tata.monitoring.infrastructure.remote

/**
 * `AlertSummaryResource` of the backend. Every field is nullable because Gson does not enforce
 * Kotlin nullability; [toDomain] validates the payload before it reaches the domain.
 */
data class AlertSummaryResponse(
    val id: Long?,
    val intakeId: String?,
    val medicationName: String?,
    val scheduledAt: String?,
    val reason: String?,
    val status: String?,
    val openedAt: String?,
    val closedAt: String?,
)
