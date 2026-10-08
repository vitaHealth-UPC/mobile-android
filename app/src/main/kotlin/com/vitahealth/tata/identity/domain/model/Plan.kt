package com.vitahealth.tata.identity.domain.model

import java.math.BigDecimal

data class Plan(
    val code: String,
    val name: String,
    val monthlyPrice: BigDecimal,
    val currency: String,
    val capabilities: Set<PlanCapability>,
) {
    fun supports(capability: PlanCapability): Boolean = capability in capabilities
}
