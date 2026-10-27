package com.vitahealth.tata.analytics.infrastructure.remote

/** Summary metrics, daily trend and recent intake outcomes. */
data class AdherenceSummaryResponse(
    val periodDays: Int,
    val scheduledCount: Int,
    val adherencePercent: Int,
    val adherenceChangePercent: Int?,
    val onTimePercent: Int,
    val onTimeChangePercent: Int?,
    val lateCount: Int,
    val omittedCount: Int,
    val trend: List<TrendPointResponse>?,
    val recentIntakes: List<RecentIntakeResponse>?,
    val pattern: SummaryPatternResponse?,
)
