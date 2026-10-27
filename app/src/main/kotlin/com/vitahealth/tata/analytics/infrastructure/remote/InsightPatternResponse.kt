package com.vitahealth.tata.analytics.infrastructure.remote

/** Detected omission or lateness pattern and its hour range. */
data class InsightPatternResponse(
    val type: String,
    val timeBand: String,
    val omittedCount: Int,
    val lateCount: Int,
    val fromHour: Int,
    val toHour: Int,
)
