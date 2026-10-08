package com.vitahealth.tata.monitoring.application.queries

data class GetOpenAlertsQuery(
    val caregiverId: String,
    val olderAdultId: String,
)

data class GetAlertDetailQuery(
    val caregiverId: String,
    val olderAdultId: String,
    val alertId: Long,
)
