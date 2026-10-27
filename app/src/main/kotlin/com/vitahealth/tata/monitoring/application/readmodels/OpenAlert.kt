package com.vitahealth.tata.monitoring.application.readmodels

import java.time.Instant

/** Open follow-up alert associated with a scheduled intake. */
data class OpenAlert(
    val id: Long,
    val medicationName: String,
    val reason: String,
    val scheduledAt: Instant,
)
