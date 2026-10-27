package com.vitahealth.tata.analytics.domain.model.valueobjects

/** Period choices available in adherence history. */
enum class AdherencePeriod(val days: Int) {
    LastWeek(7),
    LastMonth(30),
}
