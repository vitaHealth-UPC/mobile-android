package com.vitahealth.tata.analytics.infrastructure.remote

/** Counts grouped by a recurring time band. */
data class SummaryPatternResponse(val timeBand: String, val omittedCount: Int, val lateCount: Int)
