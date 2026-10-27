package com.vitahealth.tata.inventory.application.readmodels

import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import java.time.Instant

/**
 * Read side of a medication's inventory (US-41 / US-42), mapped from the backend's inventory
 * resource.
 *
 * [daysRemaining] is the backend coverage estimate, or null when no daily schedule exists.
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
    /**
     * The server records the initial stock as the first batch (US-40).
     * A single batch is therefore not evidence of a replenishment (US-43).
     * The API has no explicit batch origin; do not render a "last replenishment"
     * before at least one additional batch exists.
     */
    val lastReplenishment: InventoryBatch?
        get() = if (batches.size > 1) batches.maxByOrNull { it.registeredAt } else null
}
