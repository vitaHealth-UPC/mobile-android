package com.vitahealth.tata.monitoring.application.readmodels

import java.time.Instant

/** A historical intake without display formatting or a device-specific time zone. */
data class IntakeHistoryEntry(
    val medicationName: String,
    val scheduledAt: Instant,
    val outcome: IntakeHistoryOutcome,
)
