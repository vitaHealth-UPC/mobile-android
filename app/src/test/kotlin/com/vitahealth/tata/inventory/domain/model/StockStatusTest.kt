package com.vitahealth.tata.inventory.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StockStatusTest {
    @Test
    fun `low stock flag maps to LOW`() {
        assertEquals(StockStatus.LOW, StockStatus.fromLowStockFlag(true))
    }

    @Test
    fun `not low maps to AVAILABLE`() {
        assertEquals(StockStatus.AVAILABLE, StockStatus.fromLowStockFlag(false))
    }
}
