package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AnalyticsMappersTest {
    private val summary = AdherenceSummaryResponse(
        periodDays = 30,
        scheduledCount = 120,
        adherencePercent = 92,
        adherenceChangePercent = 8,
        onTimePercent = 86,
        onTimeChangePercent = null,
        lateCount = 6,
        omittedCount = 4,
        trend = listOf(TrendPointResponse("2026-09-20", 57)),
        recentIntakes = listOf(
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
            listOf(IntakeOutcomeStatus.OnTime, IntakeOutcomeStatus.Late, IntakeOutcomeStatus.Omitted),
            model.recentIntakes.map { it.status },
        )
        assertEquals(24, model.recentIntakes[1].minutesLate)
    }

    @Test
    fun summaryPatternIsDescribedInSpanishAndEveningCountsAsTarde() {
        val pattern = summary.toReadModel(ZoneOffset.UTC).pattern!!

        assertEquals("Excelente progreso", pattern.headline)
        assertEquals("4 omisiones y 6 tomas tardías se concentran en la tarde.", pattern.summary)
    }

    @Test
    fun lowAdherenceDoesNotCelebrateProgress() {
        val pattern = summary.copy(adherencePercent = 70).toReadModel(ZoneOffset.UTC).pattern!!
        assertEquals("Atención a los horarios", pattern.headline)
    }

    @Test
    fun summaryWithoutPatternOrExtrasMapsToEmptyValues() {
        val model = summary.copy(pattern = null, trend = null, recentIntakes = null).toReadModel(ZoneOffset.UTC)

        assertNull(model.pattern)
        assertEquals(emptyList<Any>(), model.trend)
        assertEquals(emptyList<Any>(), model.recentIntakes)
    }

    @Test
    fun unknownIntakeStatusIsRejectedInsteadOfShownWrongly() {
        val broken = summary.copy(recentIntakes = listOf(RecentIntakeResponse("2026-10-06T13:00:00Z", "X", "???", null)))
        assertThrows(IllegalArgumentException::class.java) { broken.toReadModel(ZoneOffset.UTC) }
    }

    private val insights = AdherenceInsightsResponse(
        periodDays = 30,
        pattern = InsightPatternResponse("OMISSION", "EVENING", omittedCount = 4, lateCount = 6, fromHour = 18, toHour = 21),
        concentration = listOf(listOf(0.27, 0.5), listOf(0.2, 0.8), listOf(0.4, 0.3)),
        recommendations = listOf("ADJUST_REMINDER", "REVIEW_SCHEDULE", "FOLLOW_UP_ONE_WEEK"),
    )

    @Test
    fun insightsBuildTheTitleSummaryAndRecommendationsFromCodes() {
        val model = insights.toReadModel()

        assertEquals("Patrón de omisiones por la tarde", model.patternTitle)
        assertEquals("4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.", model.patternSummary)
        assertEquals(3, model.concentration.size)
        assertEquals(
            listOf("Ajusta el recordatorio", "Revisa el horario", "Acompaña durante una semana"),
            model.recommendations.map { it.title },
        )
    }

    @Test
    fun latenessPatternsAreNamedAsTardanzasAndUnknownCodesAreIgnored() {
        val model = insights.copy(
            pattern = insights.pattern.copy(type = "LATENESS"),
            recommendations = listOf("ADJUST_REMINDER", "SOMETHING_NEW"),
        ).toReadModel()

        assertEquals("Patrón de tardanzas por la tarde", model.patternTitle)
        assertEquals(1, model.recommendations.size)
    }

    @Test
    fun hourRangesUseTheTwelveHourClock() {
        assertEquals("6:00 y 9:00 p. m.", hourRangeText(18, 21))
        assertEquals("11:00 a. m. y 2:00 p. m.", hourRangeText(11, 14))
        assertEquals("8:00 y 10:00 a. m.", hourRangeText(8, 10))
        assertEquals("10:00 p. m. y 12:00 a. m.", hourRangeText(22, 24))
    }
}
