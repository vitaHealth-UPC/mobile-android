package com.vitahealth.tata.identity.domain.model.entities

import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import java.math.BigDecimal
/** Server-defined subscription plan and the capabilities available to its account. */
data class Plan(
    val code: String,
    val name: String,
    val monthlyPrice: BigDecimal,
    val currency: String,
    val capabilities: Set<PlanCapability>,
) {
    init {
        require(code.isNotBlank()) { "plan code is required" }
        require(name.isNotBlank()) { "plan name is required" }
        require(currency.isNotBlank()) { "currency is required" }
        require(monthlyPrice.signum() >= 0) { "monthly price cannot be negative" }
    }

    fun supports(capability: PlanCapability): Boolean = capability in capabilities
}
