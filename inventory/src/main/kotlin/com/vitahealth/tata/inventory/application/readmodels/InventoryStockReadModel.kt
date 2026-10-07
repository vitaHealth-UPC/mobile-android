package com.vitahealth.tata.inventory.application.readmodels

import com.vitahealth.tata.inventory.domain.model.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.StockStatus
import java.time.Instant

/**
 * Read side of a medication's inventory (US-41 / US-42), mapped from the backend's
 * inventory resource.
 *
 * [daysRemaining] is optional and currently always `null`: the backend does not yet return a
 * "days of treatment" estimate, so the UI shows "Sin estimación". Computing it in the app would
 * be business logic that belongs in the backend.
 */
data class InventoryStockReadModel(
    val medicationId: String,
    val remainingStock: Int,
    val replenishmentThreshold: Int,
    val status: StockStatus,
    val batches: List<InventoryBatch>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val daysRemaining: Int? = null,
) {
    /** Most recent registered batch, used to render the "Última reposición" summary. */
    val lastReplenishment: InventoryBatch?
        get() = batches.maxByOrNull { it.registeredAt }
}
