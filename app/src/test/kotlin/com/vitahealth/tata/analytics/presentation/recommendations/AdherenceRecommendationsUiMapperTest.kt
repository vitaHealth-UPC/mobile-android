package com.vitahealth.tata.analytics.presentation.recommendations

import com.vitahealth.tata.analytics.application.readmodels.AdherenceInsightPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.presentation.recommendations.mappers.hourRangeText
import com.vitahealth.tata.analytics.presentation.recommendations.mappers.toUi
import org.junit.Assert.assertEquals
import org.junit.Test

class AdherenceRecommendationsUiMapperTest {
    @Test
    fun typedFactsKeepTheExistingSpanishContent() {
        val facts =
            AdherenceRecommendationsReadModel(
                periodDays = 30,
                pattern =
                    AdherenceInsightPatternReadModel(
                        AdherencePatternType.OMISSION,
                        AdherenceTimeBand.EVENING,
                        4,
                        6,
                        18,
                        21,
                    ),
                concentration = listOf(listOf(0.27f, 0.5f)),
                recommendations = AdherenceRecommendation.entries,
            )

        val model = facts.toUi()

        assertEquals("Patrón de omisiones por la tarde", model.patternTitle)
        assertEquals(
            "4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.",
            model.patternSummary,
        )
        assertEquals(facts.concentration, model.concentration)
        assertEquals(
            listOf("Ajusta el recordatorio", "Revisa el horario", "Acompaña durante una semana"),
            model.recommendations.map { it.title },
        )
    }

    @Test
    fun hourRangesKeepTheExistingTwelveHourClock() {
        assertEquals("6:00 y 9:00 p. m.", hourRangeText(18, 21))
        assertEquals("11:00 a. m. y 2:00 p. m.", hourRangeText(11, 14))
        assertEquals("8:00 y 10:00 a. m.", hourRangeText(8, 10))
        assertEquals("10:00 p. m. y 12:00 a. m.", hourRangeText(22, 24))
    }
}
