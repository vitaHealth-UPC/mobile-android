package com.vitahealth.tata.identity.infrastructure.remote

/** Account subscription payload with its current plan and renewal date. */
data class SubscriptionDto(
    val accountId: String?,
    val plan: PlanDto?,
    val status: String?,
    val renewsAt: String?,
)
