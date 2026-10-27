package com.vitahealth.tata.inventory.infrastructure.remote

/**
 * Mirrors the backend `InventoryResource`. Timestamps are ISO-8601 strings because the shared
 * Retrofit uses the default Gson, which does not deserialize `java.time.Instant`; they are parsed
 * by the mapper. Coverage is computed by the backend. Required scalar fields remain nullable at
 * this transport boundary so the mapper can reject absent JSON instead of accepting Gson defaults.
 */
data class InventoryResponse(
    val id: String,
    val medicationId: String,
    val remainingStock: Int?,
    val replenishmentThreshold: Int?,
    val lowStock: Boolean?,
    val batches: List<BatchResponse>,
    val createdAt: String,
    val updatedAt: String,
    val daysRemaining: Int? = null,
    val dailyConsumptionUnits: Int? = null,
)
