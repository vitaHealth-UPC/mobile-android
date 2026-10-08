package com.vitahealth.tata.identity.presentation.subscription

import java.math.BigDecimal
import java.math.RoundingMode

/** "S/ 19.90" for soles, "USD 19.90" for any other currency. Always two decimals with a dot. */
internal fun formatPlanPrice(price: BigDecimal, currency: String): String {
    val amount = price.setScale(2, RoundingMode.HALF_UP).toPlainString()
    val symbol = if (currency.equals("PEN", ignoreCase = true)) "S/" else currency.uppercase()
    return "$symbol $amount"
}
