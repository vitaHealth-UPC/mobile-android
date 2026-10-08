package com.vitahealth.tata.inventory.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReorderThresholdTest {
    @Test
    fun `rejects negative`() {
        assertNull(ReorderThreshold.of(-1))
    }

    @Test
    fun `accepts zero`() {
        assertEquals(0, ReorderThreshold.of(0)?.value)
    }

    @Test
    fun `accepts a positive value`() {
        assertEquals(5, ReorderThreshold.of(5)?.value)
    }
}
