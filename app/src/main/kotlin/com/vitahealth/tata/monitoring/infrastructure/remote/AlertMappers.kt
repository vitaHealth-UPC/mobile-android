package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import java.time.Instant
import java.time.OffsetDateTime

/** Maps the backend alert resource; throws [IllegalArgumentException] when the payload breaks the contract. */
fun AlertSummaryResponse.toDomain(): CaregiverAlert = CaregiverAlert(
    id = requireNotNull(id) { "alert id is missing" },
    intakeId = requireNotNull(intakeId) { "alert intake is missing" },
    medicationName = requireNotNull(medicationName) { "alert medication is missing" },
    scheduledAt = parseInstant(requireNotNull(scheduledAt) { "alert schedule is missing" }),
    reason = reason.orEmpty(),
    status = alertStatusOf(status),
    openedAt = parseInstant(requireNotNull(openedAt) { "alert opening time is missing" }),
    closedAt = closedAt?.let(::parseInstant),
)

fun alertStatusOf(value: String?): AlertStatus =
    AlertStatus.entries.firstOrNull { it.name == value }
        ?: throw IllegalArgumentException("unknown alert status: $value")

private fun parseInstant(value: String): Instant = try {
    OffsetDateTime.parse(value).toInstant()
} catch (exception: java.time.DateTimeException) {
    throw IllegalArgumentException("invalid date-time: $value", exception)
}
