package com.vitahealth.tata.inventory.application.ports

import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Application contract for reading stock and registering inventory movements.
 *
 * A `Failure` with code `INVENTORY_NOT_FOUND` from [getStock] means the medication has no inventory
 * yet (HTTP 404): the presentation layer treats it as the "not initialized" state, not as an error.
 */
interface InventoryRepository {
    /** Returns current stock or INVENTORY_NOT_FOUND when it has not been initialized. */
    suspend fun getStock(medicationId: String): AppResult<InventoryStockReadModel>

    /** Initializes stock with a positive quantity and a nonnegative replenishment threshold. */
    suspend fun registerInitialInventory(
        medicationId: String,
        initialQuantity: Int,
        replenishmentThreshold: Int,
    ): AppResult<InventoryStockReadModel>

    /** Adds positive units, retaining optional normalized lot metadata. */
    suspend fun registerReplenishment(
        medicationId: String,
        quantity: Int,
        lot: String? = null,
    ): AppResult<InventoryStockReadModel>
}
