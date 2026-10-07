package com.vitahealth.tata.identity.domain.model

import java.time.Instant

data class Subscription(
    val accountId: String,
    val plan: Plan,
    val status: SubscriptionStatus,
    /** Null when the backend does not give a renewal date. */
    val renewsAt: Instant?,
) {
    fun isOn(plan: Plan): Boolean = this.plan.code == plan.code

    /** What the account would gain and lose by moving to [target]. */
    fun impactOfChangingTo(target: Plan): PlanChangeImpact = PlanChangeImpact(
        gained = target.capabilities - plan.capabilities,
        lost = plan.capabilities - target.capabilities,
    )
}

data class PlanChangeImpact(
    val gained: Set<PlanCapability>,
    val lost: Set<PlanCapability>,
)
