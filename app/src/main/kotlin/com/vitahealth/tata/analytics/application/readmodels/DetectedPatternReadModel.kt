package com.vitahealth.tata.analytics.application.readmodels

import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand

/** Counts and time band of a recurring adherence pattern, independent of display language. */
data class DetectedPatternReadModel(
    val timeBand: AdherenceTimeBand,
    val omittedCount: Int,
    val lateCount: Int,
)
