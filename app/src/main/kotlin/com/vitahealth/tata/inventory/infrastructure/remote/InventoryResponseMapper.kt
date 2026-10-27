package com.vitahealth.tata.inventory.infrastructure.remote

import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import java.time.Instant

/** Validates the stock resource before exposing it to application and presentation code. */
internal fun InventoryResponse.toReadModel(expectedMedicationId: String): InventoryStockReadModel {
    require(id.isNotBlank() && medicationId == expectedMedicationId)
    val remainingStock = requireNotNull(remainingStock)
    val replenishmentThreshold = requireNotNull(replenishmentThreshold)
    val lowStock = requireNotNull(lowStock)
    require(remainingStock >= 0 && replenishmentThreshold >= 0)
    require(daysRemaining == null || daysRemaining >= 0)
    require(dailyConsumptionUnits == null || dailyConsumptionUnits >= 0)
    require(batches.map { it.id }.distinct().size == batches.size)
    val mappedBatches =
        batches.map { batch ->
            require(batch.id.isNotBlank() && batch.quantity > 0)
            InventoryBatch(
                id = batch.id,
                quantity = batch.quantity,
                registeredAt = Instant.parse(batch.registeredAt),
                lot = batch.lot,
            )
        }
    return InventoryStockReadModel(
        medicationId = medicationId,
        remainingStock = remainingStock,
        replenishmentThreshold = replenishmentThreshold,
        status = StockStatus.fromLowStockFlag(lowStock),
        batches = mappedBatches,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        daysRemaining = daysRemaining,
    )
}
