package com.vitahealth.tata.analytics.infrastructure.remote

/** Recent intake outcome and scheduled instant. */
data class RecentIntakeResponse(
    val scheduledAt: String,
    val medicationName: String,
    val status: String,
    val minutesLate: Int?,
)
