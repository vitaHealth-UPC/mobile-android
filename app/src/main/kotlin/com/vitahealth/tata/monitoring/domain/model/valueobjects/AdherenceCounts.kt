package com.vitahealth.tata.monitoring.domain.model.valueobjects

/** Validated adherence counts; absence of evidence has no percentage. */
data class AdherenceCounts(val confirmed: Int, val total: Int) {
    init {
        require(confirmed >= 0 && total >= confirmed)
    }

    val percentage: Double?
        get() = if (total == 0) null else confirmed * 100.0 / total
}
