package com.vitahealth.tata.identity.presentation.subscription

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanPriceFormatTest {
    @Test
    fun solesAreShownWithTheSolSign() {
        assertEquals("S/ 19.90", formatPlanPrice(BigDecimal("19.90"), "PEN"))
        assertEquals("S/ 9.90", formatPlanPrice(BigDecimal("9.9"), "pen"))
    }

    @Test
    fun anyOtherCurrencyUsesItsCode() {
        assertEquals("USD 4.99", formatPlanPrice(BigDecimal("4.99"), "usd"))
    }

    @Test
    fun theAmountAlwaysHasTwoDecimals() {
        assertEquals("S/ 20.00", formatPlanPrice(BigDecimal("20"), "PEN"))
        assertEquals("S/ 10.00", formatPlanPrice(BigDecimal("9.999"), "PEN"))
    }
}
