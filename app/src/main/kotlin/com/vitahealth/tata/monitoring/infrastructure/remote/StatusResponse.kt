package com.vitahealth.tata.monitoring.infrastructure.remote

/** Monitoring status with weekly adherence and open alerts. */
data class StatusResponse(
    val weeklyAdherence: AdherenceResponse,
    val openAlerts: List<AlertSummaryResponse>?,
)
