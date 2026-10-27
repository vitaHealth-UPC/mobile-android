package com.vitahealth.tata.analytics.application.readmodels

import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand

/** Pattern facts used to explain adherence difficulties without prescribing display text. */
data class AdherenceInsightPatternReadModel(
    val type: AdherencePatternType,
    val timeBand: AdherenceTimeBand,
    val omittedCount: Int,
    val lateCount: Int,
    val fromHour: Int,
    val toHour: Int,
)
