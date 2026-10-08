package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceTrendPointReadModel
import com.vitahealth.tata.analytics.application.readmodels.DetectedPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecentIntakeReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecommendationReadModel
import com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private const val EXCELLENT_ADHERENCE_PERCENT = 90

internal fun AdherenceSummaryResponse.toReadModel(zone: ZoneId = ZoneId.systemDefault()): AdherenceSummaryReadModel =
    AdherenceSummaryReadModel(
        periodDays = periodDays,
        scheduledCount = scheduledCount,
        adherencePercent = adherencePercent,
        adherenceChangePercent = adherenceChangePercent,
        onTimePercent = onTimePercent,
        onTimeChangePercent = onTimeChangePercent,
        lateCount = lateCount,
        omittedCount = omittedCount,
        trend = trend.orEmpty().map {
            AdherenceTrendPointReadModel(date = LocalDate.parse(it.date), adherencePercent = it.adherencePercent)
        },
        recentIntakes = recentIntakes.orEmpty().map { it.toReadModel(zone) },
        pattern = pattern?.let {
            DetectedPatternReadModel(
                headline = if (adherencePercent >= EXCELLENT_ADHERENCE_PERCENT) "Excelente progreso" else "Atención a los horarios",
                summary = "${it.omittedCount} omisiones y ${it.lateCount} tomas tardías se concentran en la ${timeBandText(it.timeBand)}.",
            )
        },
    )

internal fun RecentIntakeResponse.toReadModel(zone: ZoneId): RecentIntakeReadModel =
    RecentIntakeReadModel(
        scheduledAt = LocalDateTime.ofInstant(Instant.parse(scheduledAt), zone),
        medicationName = medicationName,
        status = when (status) {
            "CONFIRMED" -> IntakeOutcomeStatus.OnTime
            "LATE" -> IntakeOutcomeStatus.Late
            "OMITTED" -> IntakeOutcomeStatus.Omitted
            else -> throw IllegalArgumentException("Unknown intake status: $status")
        },
        minutesLate = minutesLate,
    )

internal fun AdherenceInsightsResponse.toReadModel(): AdherenceRecommendationsReadModel {
    val kind = if (pattern.type == "LATENESS") "tardanzas" else "omisiones"
    return AdherenceRecommendationsReadModel(
        periodDays = periodDays,
        patternTitle = "Patrón de $kind por la ${timeBandText(pattern.timeBand)}",
        patternSummary = endSentence(
            "${pattern.omittedCount} omisiones y ${pattern.lateCount} tomas tardías se concentran " +
                "entre ${hourRangeText(pattern.fromHour, pattern.toHour)}",
        ),
        concentration = concentration.map { row -> row.map { it.toFloat() } },
        recommendations = recommendations.mapNotNull(::recommendationOf),
    )
}

/** Evening (18-22 h) is called "tarde" too, as in the approved design ("por la tarde, entre 6:00 y 9:00 p. m."). */
internal fun timeBandText(timeBand: String): String =
    when (timeBand) {
        "MORNING" -> "mañana"
        "AFTERNOON", "EVENING" -> "tarde"
        else -> "noche"
    }

internal fun hourRangeText(fromHour: Int, toHour: Int): String {
    val from = hourText(fromHour)
    val to = hourText(toHour)
    val fromSuffix = suffixOf(fromHour)
    val toSuffix = suffixOf(toHour)
    return if (fromSuffix == toSuffix) "$from y $to $toSuffix" else "$from $fromSuffix y $to $toSuffix"
}

/** "p. m." already ends with a period, so only add one when the sentence does not end with it. */
private fun endSentence(text: String): String = if (text.endsWith(".")) text else "$text."

private fun hourText(hour: Int): String {
    val twelve = hour % 12
    return "${if (twelve == 0) 12 else twelve}:00"
}

private fun suffixOf(hour: Int): String = if (hour % 24 < 12) "a. m." else "p. m."

internal fun recommendationOf(code: String): RecommendationReadModel? =
    when (code) {
        "ADJUST_REMINDER" -> RecommendationReadModel(
            title = "Ajusta el recordatorio",
            description = "Prueba avisar 15 minutos antes de la toma.",
        )
        "REVIEW_SCHEDULE" -> RecommendationReadModel(
            title = "Revisa el horario",
            description = "Elige una hora asociada a una rutina estable.",
        )
        "FOLLOW_UP_ONE_WEEK" -> RecommendationReadModel(
            title = "Acompaña durante una semana",
            description = "Comprueba si el nuevo horario reduce retrasos.",
        )
        else -> null
    }
