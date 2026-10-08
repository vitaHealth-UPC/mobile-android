package com.vitahealth.tata.inventory.domain.model

import java.time.Instant

/**
 * A quantity of units added to an inventory at a given moment: the initial stock (US-40)
 * or a later replenishment (US-43).
 *
 * Optional lot metadata is preserved with the replenishment.
 */
data class InventoryBatch(
    val id: String,
    val quantity: Int,
    val registeredAt: Instant,
    val lot: String? = null,
)
