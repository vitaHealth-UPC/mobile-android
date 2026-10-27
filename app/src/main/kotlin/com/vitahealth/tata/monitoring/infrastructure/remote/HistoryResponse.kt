package com.vitahealth.tata.monitoring.infrastructure.remote

/** Medication, scheduled instant and outcome of a historical intake. */
data class HistoryResponse(val medicationName: String, val scheduledAt: String, val status: String)
