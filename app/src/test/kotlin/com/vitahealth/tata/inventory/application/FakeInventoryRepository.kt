package com.vitahealth.tata.inventory.application

import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

/** Configurable test double that records how it was called. */
class FakeInventoryRepository : InventoryRepository {
    var stockResult: AppResult<InventoryStockReadModel> = AppResult.Success(sampleReadModel())
    var initialResult: AppResult<InventoryStockReadModel> = AppResult.Success(sampleReadModel())
    var replenishmentResult: AppResult<InventoryStockReadModel> = AppResult.Success(sampleReadModel())

    var getStockCalls = 0
    var initialCalls = 0
    var replenishmentCalls = 0

    var lastGetStockMedicationId: String? = null
    var lastInitialMedicationId: String? = null
    var lastInitialQuantity: Int? = null
    var lastInitialThreshold: Int? = null
    var lastReplenishmentMedicationId: String? = null
    var lastReplenishmentQuantity: Int? = null

    override suspend fun getStock(medicationId: String): AppResult<InventoryStockReadModel> {
        getStockCalls++
        lastGetStockMedicationId = medicationId
        return stockResult
    }

    override suspend fun registerInitialInventory(
        medicationId: String,
        initialQuantity: Int,
        replenishmentThreshold: Int,
    ): AppResult<InventoryStockReadModel> {
        initialCalls++
        lastInitialMedicationId = medicationId
        lastInitialQuantity = initialQuantity
        lastInitialThreshold = replenishmentThreshold
        return initialResult
    }

    override suspend fun registerReplenishment(
        medicationId: String,
        quantity: Int,
        lot: String?,
    ): AppResult<InventoryStockReadModel> {
        replenishmentCalls++
        lastReplenishmentMedicationId = medicationId
        lastReplenishmentQuantity = quantity
        return replenishmentResult
    }

    companion object {
        fun sampleReadModel(
            remainingStock: Int = 5,
            status: StockStatus = StockStatus.LOW,
        ) = InventoryStockReadModel(
            medicationId = "med-1",
            remainingStock = remainingStock,
            replenishmentThreshold = 5,
            status = status,
            batches = emptyList(),
            createdAt = Instant.parse("2026-08-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-09-02T10:00:00Z"),
            daysRemaining = null,
        )
    }
}
