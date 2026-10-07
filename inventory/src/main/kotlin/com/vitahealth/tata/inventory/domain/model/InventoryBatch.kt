package com.vitahealth.tata.inventory.domain.model

import java.time.Instant

/**
 * A quantity of units added to an inventory at a given moment: the initial stock (US-40)
 * or a later replenishment (US-43).
 *
 * The backend `Batch` currently exposes only quantity and timestamp. The "Lote / nota" field
 * from the mockup is intentionally absent until the backend contract adds `lot`.
 */
data class InventoryBatch(
    val id: String,
    val quantity: Int,
    val registeredAt: Instant,
)
