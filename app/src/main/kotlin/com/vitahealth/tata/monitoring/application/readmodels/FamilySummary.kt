package com.vitahealth.tata.monitoring.application.readmodels

import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import java.time.LocalDate

/** Daily family monitoring snapshot combining adherence, agenda, stock and alerts. */
data class FamilySummary(
    val olderAdultName: String,
    val date: LocalDate,
    val weekly: AdherenceCounts,
    val today: AdherenceCounts,
    val nextDose: MonitoredDose?,
    val stockAttention: List<StockAttention>,
    val openAlerts: List<OpenAlert>,
    val inventoryAvailable: Boolean,
)
