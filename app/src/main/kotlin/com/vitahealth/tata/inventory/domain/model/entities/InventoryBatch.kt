package com.vitahealth.tata.inventory.domain.model.entities

import com.vitahealth.tata.inventory.domain.model.valueobjects.InventoryLot
import com.vitahealth.tata.inventory.domain.model.valueobjects.Quantity
import java.time.Instant

/**
 * A quantity of units added to an inventory at a given moment: the initial stock (US-40) or a later
 * replenishment (US-43).
 *
 * Optional lot metadata is preserved with the replenishment.
 */
data class InventoryBatch(
    val id: String,
    val quantity: Int,
    val registeredAt: Instant,
    val lot: String? = null,
) {
    init {
        require(id.isNotBlank()) { "A batch requires an identity" }
        require(Quantity.of(quantity) != null) { "A batch quantity must be positive" }
        require(lot == null || InventoryLot.of(lot) != null) {
            "A batch lot must be nonblank and no longer than 200 characters"
        }
    }
}
