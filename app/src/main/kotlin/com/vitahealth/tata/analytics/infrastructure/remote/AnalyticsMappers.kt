package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.application.readmodels.AdherenceInsightPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceTrendPointReadModel
import com.vitahealth.tata.analytics.application.readmodels.DetectedPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecentIntakeReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal fun AdherenceSummaryResponse.toReadModel(
    zone: ZoneId = ZoneId.systemDefault()
): AdherenceSummaryReadModel =
    AdherenceSummaryReadModel(
        periodDays = periodDays,
        scheduledCount = scheduledCount,
        adherencePercent = adherencePercent,
        adherenceChangePercent = adherenceChangePercent,
        onTimePercent = onTimePercent,
        onTimeChangePercent = onTimeChangePercent,
        lateCount = lateCount,
        omittedCount = omittedCount,
        trend =
            trend.orEmpty().map {
                AdherenceTrendPointReadModel(
                    date = LocalDate.parse(it.date),
                    adherencePercent = it.adherencePercent,
                )
            },
        recentIntakes = recentIntakes.orEmpty().map { it.toReadModel(zone) },
        pattern =
            pattern?.let {
                DetectedPatternReadModel(
                    timeBand = AdherenceTimeBand.valueOf(it.timeBand),
                    omittedCount = it.omittedCount,
                    lateCount = it.lateCount,
                )
            },
    )

internal fun RecentIntakeResponse.toReadModel(zone: ZoneId): RecentIntakeReadModel =
    RecentIntakeReadModel(
        scheduledAt = LocalDateTime.ofInstant(Instant.parse(scheduledAt), zone),
        medicationName = medicationName,
        status =
            when (status) {
                "CONFIRMED" -> IntakeOutcomeStatus.OnTime
                "LATE" -> IntakeOutcomeStatus.Late
                "OMITTED" -> IntakeOutcomeStatus.Omitted
                else -> throw IllegalArgumentException("Unknown intake status: $status")
            },
        minutesLate = minutesLate,
    )

/**
 * Converts transport codes into domain values while tolerating new optional recommendation codes.
 */
internal fun AdherenceInsightsResponse.toReadModel(): AdherenceRecommendationsReadModel =
    AdherenceRecommendationsReadModel(
        periodDays = periodDays,
        pattern =
            AdherenceInsightPatternReadModel(
                type = AdherencePatternType.valueOf(pattern.type),
                timeBand = AdherenceTimeBand.valueOf(pattern.timeBand),
                omittedCount = pattern.omittedCount,
                lateCount = pattern.lateCount,
                fromHour = pattern.fromHour,
                toHour = pattern.toHour,
            ),
        concentration = concentration.map { row -> row.map { it.toFloat() } },
        recommendations =
            recommendations.mapNotNull { code ->
                AdherenceRecommendation.entries.find { it.name == code }
            },
    )
