package com.vitahealth.tata.inventory.application

import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Inventory read/write operations backed by the web-services Inventory API.
 *
 * A `Failure` with code `INVENTORY_NOT_FOUND` from [getStock] means the medication has no
 * inventory yet (HTTP 404): the presentation layer treats it as the "not initialized" state,
 * not as an error.
 */
interface InventoryRepository {
    suspend fun getStock(medicationId: String): AppResult<InventoryStockReadModel>

    suspend fun registerInitialInventory(
        medicationId: String,
        initialQuantity: Int,
        replenishmentThreshold: Int,
    ): AppResult<InventoryStockReadModel>

    suspend fun registerReplenishment(
        medicationId: String,
        quantity: Int,
    ): AppResult<InventoryStockReadModel>
}
