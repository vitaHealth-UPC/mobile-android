package com.vitahealth.tata.analytics.application.readmodels

import java.time.LocalDate

data class AdherenceTrendPointReadModel(
    val date: LocalDate,
    val adherencePercent: Int,
)

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
)
