package com.vitahealth.tata.identity.infrastructure.remote

import java.math.BigDecimal
/** Plan catalog payload received from the subscription API. */
data class PlanDto(
    val code: String?,
    val name: String?,
    val monthlyPrice: BigDecimal?,
    val currency: String?,
    val capabilities: List<String>?,
)
