package com.vitahealth.tata.analytics.application.readmodels

/** Summary figures and recent outcomes for the requested adherence period. */
data class AdherenceSummaryReadModel(
    val periodDays: Int,
    val scheduledCount: Int,
    val adherencePercent: Int,
    val adherenceChangePercent: Int?,
    val onTimePercent: Int,
    val onTimeChangePercent: Int?,
    val lateCount: Int,
    val omittedCount: Int,
    val trend: List<AdherenceTrendPointReadModel>,
    val recentIntakes: List<RecentIntakeReadModel> = emptyList(),
    val pattern: DetectedPatternReadModel? = null,
)
