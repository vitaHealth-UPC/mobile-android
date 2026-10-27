package com.vitahealth.tata.monitoring.infrastructure.remote

/** Stock availability for a medication included in the family summary. */
data class InventoryResponse(
    val medicationId: String,
    val remainingStock: Int?,
    val lowStock: Boolean?,
)
