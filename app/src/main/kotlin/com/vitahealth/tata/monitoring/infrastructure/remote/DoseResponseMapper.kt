package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import java.time.Instant

/** Validates scheduled dose evidence before it participates in family monitoring calculations. */
internal fun DoseResponse.toMonitoredDose(): MonitoredDose {
    require(listOf(id, medicationId, medicationName, dose).all { !it.isNullOrBlank() })
    require(status in setOf("PENDING", "CONFIRMED", "LATE", "OMITTED"))
    return MonitoredDose(id, medicationId, medicationName, dose, Instant.parse(scheduledAt), status)
}
