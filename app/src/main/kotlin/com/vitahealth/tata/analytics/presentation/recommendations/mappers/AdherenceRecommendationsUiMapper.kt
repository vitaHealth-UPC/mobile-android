package com.vitahealth.tata.analytics.presentation.recommendations.mappers

import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.presentation.recommendations.ui.AdherenceRecommendationsUi
import com.vitahealth.tata.analytics.presentation.recommendations.ui.RecommendationUi

/** Builds the existing Spanish presentation from language-independent analytics facts. */
internal fun AdherenceRecommendationsReadModel.toUi(): AdherenceRecommendationsUi {
    val kind =
        when (pattern.type) {
            AdherencePatternType.LATENESS -> "tardanzas"
            AdherencePatternType.OMISSION -> "omisiones"
        }
    return AdherenceRecommendationsUi(
        periodDays = periodDays,
        patternTitle = "Patrón de $kind por la ${timeBandText(pattern.timeBand)}",
        patternSummary =
            endSentence(
                "${pattern.omittedCount} omisiones y ${pattern.lateCount} tomas tardías se concentran " +
                    "entre ${hourRangeText(pattern.fromHour, pattern.toHour)}"
            ),
        concentration = concentration,
        recommendations = recommendations.map { it.toUi() },
    )
}

/**
 * Evening (18-22 h) is called "tarde" too, as in the approved design ("por la tarde, entre 6:00 y
 * 9:00 p. m.").
 */
private fun timeBandText(timeBand: AdherenceTimeBand): String =
    when (timeBand) {
        AdherenceTimeBand.MORNING -> "mañana"
        AdherenceTimeBand.AFTERNOON,
        AdherenceTimeBand.EVENING -> "tarde"
        AdherenceTimeBand.NIGHT -> "noche"
    }

internal fun hourRangeText(fromHour: Int, toHour: Int): String {
    val from = hourText(fromHour)
    val to = hourText(toHour)
    val fromSuffix = suffixOf(fromHour)
    val toSuffix = suffixOf(toHour)
    return if (fromSuffix == toSuffix) "$from y $to $toSuffix"
    else "$from $fromSuffix y $to $toSuffix"
}

/** "p. m." already ends with a period, so only add one when the sentence does not end with it. */
private fun endSentence(text: String): String = if (text.endsWith(".")) text else "$text."

private fun hourText(hour: Int): String {
    val twelve = hour % 12
    return "${if (twelve == 0) 12 else twelve}:00"
}

private fun suffixOf(hour: Int): String = if (hour % 24 < 12) "a. m." else "p. m."

private fun AdherenceRecommendation.toUi(): RecommendationUi =
    when (this) {
        AdherenceRecommendation.ADJUST_REMINDER ->
            RecommendationUi(
                title = "Ajusta el recordatorio",
                description = "Prueba avisar 15 minutos antes de la toma.",
            )
        AdherenceRecommendation.REVIEW_SCHEDULE ->
            RecommendationUi(
                title = "Revisa el horario",
                description = "Elige una hora asociada a una rutina estable.",
            )
        AdherenceRecommendation.FOLLOW_UP_ONE_WEEK ->
            RecommendationUi(
                title = "Acompaña durante una semana",
                description = "Comprueba si el nuevo horario reduce retrasos.",
            )
    }
