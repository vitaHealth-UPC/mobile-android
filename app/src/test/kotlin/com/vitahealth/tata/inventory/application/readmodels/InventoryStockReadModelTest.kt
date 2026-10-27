package com.vitahealth.tata.inventory.application.readmodels

import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Cross-contract semantics: the first batch is initial stock, not a replenishment. */
class InventoryStockReadModelTest {
    private val initialTime = Instant.parse("2026-10-01T10:00:00Z")

    private fun stock(batches: List<InventoryBatch>): InventoryStockReadModel =
        InventoryStockReadModel(
            medicationId = "medication-1",
            remainingStock = 15,
            replenishmentThreshold = 3,
            status = StockStatus.AVAILABLE,
            batches = batches,
            createdAt = initialTime,
            updatedAt = Instant.parse("2026-10-25T10:00:00Z"),
        )

    @Test
    fun initialInventoryNeverAppearsAsLastReplenishment() {
        val initial = InventoryBatch("initial", 15, initialTime)
        assertNull(stock(listOf(initial)).lastReplenishment)
        assertNull(stock(emptyList()).lastReplenishment)
    }

    @Test
    fun additionalBatchAppearsAsLatestReplenishment() {
        val initial = InventoryBatch("initial", 15, initialTime)
        val older = InventoryBatch("replenishment-1", 10, Instant.parse("2026-10-05T10:00:00Z"))
        val newer = InventoryBatch("replenishment-2", 7, Instant.parse("2026-10-24T10:00:00Z"), "LOT-2")
        assertEquals(newer, stock(listOf(initial, newer, older)).lastReplenishment)
    }
}
