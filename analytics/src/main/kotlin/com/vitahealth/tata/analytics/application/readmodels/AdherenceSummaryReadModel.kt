package com.vitahealth.tata.analytics.application.readmodels

import com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class AdherenceTrendPointReadModel(
    val date: LocalDate,
    val adherencePercent: Int,
)

data class RecentIntakeReadModel(
    val scheduledAt: LocalDateTime,
    val medicationName: String,
    val status: IntakeOutcomeStatus,
    val minutesLate: Int?,
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
    val recentIntakes: List<RecentIntakeReadModel> = emptyList(),
)
