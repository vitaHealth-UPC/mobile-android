package com.vitahealth.tata.analytics.presentation.history

import com.vitahealth.tata.analytics.application.readmodels.DetectedPatternReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.presentation.history.mappers.toUi
import org.junit.Assert.assertEquals
import org.junit.Test

class AdherencePatternUiMapperTest {
    @Test
    fun eveningKeepsTheApprovedSpanishDescription() {
        val pattern = DetectedPatternReadModel(AdherenceTimeBand.EVENING, 4, 6).toUi(92)

        assertEquals("Excelente progreso", pattern.headline)
        assertEquals("4 omisiones y 6 tomas tardías se concentran en la tarde.", pattern.summary)
    }

    @Test
    fun belowTheProgressThresholdTheHeadlineCallsAttentionToSchedules() {
        val pattern = DetectedPatternReadModel(AdherenceTimeBand.MORNING, 4, 6)

        assertEquals("Atención a los horarios", pattern.toUi(89).headline)
        assertEquals("Excelente progreso", pattern.toUi(90).headline)
    }
}
