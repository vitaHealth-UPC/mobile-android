package com.vitahealth.tata.analytics.presentation.history.mappers

import com.vitahealth.tata.analytics.application.readmodels.DetectedPatternReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.presentation.history.ui.PatternUi

private const val EXCELLENT_ADHERENCE_PERCENT = 90

/** Formats a detected pattern for the Spanish history screen. */
internal fun DetectedPatternReadModel.toUi(adherencePercent: Int): PatternUi {
    val timeBandLabel =
        when (timeBand) {
            AdherenceTimeBand.MORNING -> "mañana"
            AdherenceTimeBand.AFTERNOON,
            AdherenceTimeBand.EVENING -> "tarde"
            AdherenceTimeBand.NIGHT -> "noche"
        }
    return PatternUi(
        headline =
            if (adherencePercent >= EXCELLENT_ADHERENCE_PERCENT) {
                "Excelente progreso"
            } else {
                "Atención a los horarios"
            },
        summary =
            "$omittedCount omisiones y $lateCount tomas tardías se concentran en la $timeBandLabel.",
    )
}
