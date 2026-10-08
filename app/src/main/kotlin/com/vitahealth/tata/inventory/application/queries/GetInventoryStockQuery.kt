package com.vitahealth.tata.inventory.application.queries

/** US-41 / US-42: read the remaining stock, threshold, low-stock status and batches. */
data class GetInventoryStockQuery(
    val medicationId: String,
)
