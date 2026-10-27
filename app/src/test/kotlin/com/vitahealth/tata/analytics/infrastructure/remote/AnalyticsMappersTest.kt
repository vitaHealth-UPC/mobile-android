package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AnalyticsMappersTest {
    private val summary =
        AdherenceSummaryResponse(
            periodDays = 30,
            scheduledCount = 120,
            adherencePercent = 92,
            adherenceChangePercent = 8,
            onTimePercent = 86,
            onTimeChangePercent = null,
            lateCount = 6,
            omittedCount = 4,
            trend = listOf(TrendPointResponse("2026-09-20", 57)),
            recentIntakes =
                listOf(
                    RecentIntakeResponse("2026-10-06T13:00:00Z", "Losartán", "CONFIRMED", null),
                    RecentIntakeResponse("2026-10-05T01:00:00Z", "Amlodipino", "LATE", 24),
                    RecentIntakeResponse("2026-10-03T18:00:00Z", "Vitamina D3", "OMITTED", null),
                ),
            pattern = SummaryPatternResponse("EVENING", omittedCount = 4, lateCount = 6),
        )

    @Test
    fun summaryKeepsTheFiguresAndConvertsDatesToTheDeviceZone() {
        val model = summary.toReadModel(ZoneOffset.UTC)

        assertEquals(92, model.adherencePercent)
        assertEquals(8, model.adherenceChangePercent)
        assertNull(model.onTimeChangePercent)
        assertEquals(LocalDate.of(2026, 9, 20), model.trend.single().date)
        assertEquals(LocalDateTime.of(2026, 10, 6, 13, 0), model.recentIntakes[0].scheduledAt)
        assertEquals(
            listOf(
                IntakeOutcomeStatus.OnTime,
                IntakeOutcomeStatus.Late,
                IntakeOutcomeStatus.Omitted,
            ),
            model.recentIntakes.map { it.status },
        )
        assertEquals(24, model.recentIntakes[1].minutesLate)
    }

    @Test
    fun summaryPatternPreservesTheTimeBandAndCountsWithoutDisplayText() {
        val pattern = summary.toReadModel(ZoneOffset.UTC).pattern!!

        assertEquals(AdherenceTimeBand.EVENING, pattern.timeBand)
        assertEquals(4, pattern.omittedCount)
        assertEquals(6, pattern.lateCount)
    }

    @Test
    fun summaryWithoutPatternOrExtrasMapsToEmptyValues() {
        val model =
            summary
                .copy(pattern = null, trend = null, recentIntakes = null)
                .toReadModel(ZoneOffset.UTC)

        assertNull(model.pattern)
        assertEquals(emptyList<Any>(), model.trend)
        assertEquals(emptyList<Any>(), model.recentIntakes)
    }

    @Test
    fun unknownIntakeStatusIsRejectedInsteadOfShownWrongly() {
        val broken =
            summary.copy(
                recentIntakes =
                    listOf(RecentIntakeResponse("2026-10-06T13:00:00Z", "X", "???", null))
            )
        assertThrows(IllegalArgumentException::class.java) { broken.toReadModel(ZoneOffset.UTC) }
    }

    private val insights =
        AdherenceInsightsResponse(
            periodDays = 30,
            pattern =
                InsightPatternResponse(
                    "OMISSION",
                    "EVENING",
                    omittedCount = 4,
                    lateCount = 6,
                    fromHour = 18,
                    toHour = 21,
                ),
            concentration = listOf(listOf(0.27, 0.5), listOf(0.2, 0.8), listOf(0.4, 0.3)),
            recommendations = listOf("ADJUST_REMINDER", "REVIEW_SCHEDULE", "FOLLOW_UP_ONE_WEEK"),
        )

    @Test
    fun insightsPreservePatternFactsAndKnownRecommendationActions() {
        val model =
            insights
                .copy(
                    pattern = insights.pattern.copy(type = "LATENESS"),
                    recommendations = listOf("ADJUST_REMINDER", "SOMETHING_NEW"),
                )
                .toReadModel()

        assertEquals(AdherencePatternType.LATENESS, model.pattern.type)
        assertEquals(AdherenceTimeBand.EVENING, model.pattern.timeBand)
        assertEquals(18, model.pattern.fromHour)
        assertEquals(21, model.pattern.toHour)
        assertEquals(3, model.concentration.size)
        assertEquals(listOf(AdherenceRecommendation.ADJUST_REMINDER), model.recommendations)
    }

    @Test
    fun unknownPatternsAndTimeBandsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            insights.copy(pattern = insights.pattern.copy(type = "UNKNOWN")).toReadModel()
        }
        assertThrows(IllegalArgumentException::class.java) {
            insights.copy(pattern = insights.pattern.copy(timeBand = "UNKNOWN")).toReadModel()
        }
        assertThrows(IllegalArgumentException::class.java) {
            summary
                .copy(pattern = summary.pattern!!.copy(timeBand = "UNKNOWN"))
                .toReadModel(ZoneOffset.UTC)
        }
        assertEquals(
            AdherenceTimeBand.NIGHT,
            insights
                .copy(pattern = insights.pattern.copy(timeBand = "NIGHT"))
                .toReadModel()
                .pattern
                .timeBand,
        )
    }
}
