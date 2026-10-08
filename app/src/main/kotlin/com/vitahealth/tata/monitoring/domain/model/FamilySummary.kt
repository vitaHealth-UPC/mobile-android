package com.vitahealth.tata.monitoring.domain.model

import java.time.Instant
import java.time.LocalDate

data class AdherenceCounts(val confirmed: Int, val total: Int) {
    init { require(confirmed >= 0 && total >= confirmed) }
    val percentage: Double? get() = if (total == 0) null else confirmed * 100.0 / total
}
data class MonitoredDose(val id: String, val medicationId: String, val medicationName: String,
    val dose: String, val scheduledAt: Instant, val status: String)
data class StockAttention(val medicationId: String, val medicationName: String, val remaining: Int)
data class OpenAlert(val id: Long, val medicationName: String, val reason: String, val scheduledAt: Instant)
data class FamilySummary(val olderAdultName: String, val date: LocalDate, val weekly: AdherenceCounts,
    val today: AdherenceCounts, val nextDose: MonitoredDose?, val stockAttention: List<StockAttention>,
    val openAlerts: List<OpenAlert>, val inventoryAvailable: Boolean)
