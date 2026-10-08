package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.InventoryRepository
import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQuery
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * US-41 / US-42. A `Failure` with code `INVENTORY_NOT_FOUND` is a valid outcome meaning the
 * medication has no inventory yet; the ViewModel renders the "not initialized" state from it.
 */
class GetInventoryStockQueryHandler(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        query: GetInventoryStockQuery,
    ): AppResult<InventoryStockReadModel> {
        val medicationId = query.medicationId.trim()
        if (medicationId.isBlank()) {
            return AppResult.Failure(
                message = "medicationId is required",
                code = "INVALID_MEDICATION_REFERENCE",
            )
        }

        return repository.getStock(medicationId)
    }
}
