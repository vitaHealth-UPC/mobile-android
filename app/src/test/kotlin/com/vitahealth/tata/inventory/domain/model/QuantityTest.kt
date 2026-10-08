package com.vitahealth.tata.inventory.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuantityTest {
    @Test
    fun `rejects zero`() {
        assertNull(Quantity.of(0))
    }

    @Test
    fun `rejects negative`() {
        assertNull(Quantity.of(-3))
    }

    @Test
    fun `accepts a positive value`() {
        assertEquals(30, Quantity.of(30)?.value)
    }
}
