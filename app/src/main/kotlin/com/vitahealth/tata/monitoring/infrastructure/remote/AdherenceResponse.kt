package com.vitahealth.tata.monitoring.infrastructure.remote

/** Weekly confirmed and total intake counts returned by monitoring. */
data class AdherenceResponse(val confirmedIntakes: Int?, val totalIntakes: Int?)
