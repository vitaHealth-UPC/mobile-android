package com.vitahealth.tata.monitoring.application.readmodels

import java.time.Instant

/** Scheduled medication dose exposed to the family monitoring view. */
data class MonitoredDose(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String,
    val scheduledAt: Instant,
    val status: String,
)
