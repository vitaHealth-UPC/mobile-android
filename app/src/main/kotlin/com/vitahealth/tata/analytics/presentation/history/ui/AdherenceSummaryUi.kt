package com.vitahealth.tata.analytics.presentation.history.ui

/** Summary figures and formatted history content displayed by the screen. */
data class AdherenceSummaryUi(
    val periodLabel: String,
    val adherencePercent: Int,
    val adherenceChangeText: String?,
    val onTimePercent: Int,
    val onTimeChangeText: String?,
    val lateCount: Int,
    val omittedCount: Int,
    val lateOmittedCaption: String,
    val trend: List<AdherenceTrendPoint>,
    val recentIntakes: List<RecentIntakeUi> = emptyList(),
    val pattern: PatternUi? = null,
)
