package com.vitahealth.tata.identity.domain.model.entities

import com.vitahealth.tata.identity.domain.model.valueobjects.PlanChangeImpact
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import java.time.Instant
/** Current account plan, lifecycle status and optional renewal time. */
data class Subscription(
    val accountId: String,
    val plan: Plan,
    val status: SubscriptionStatus,
    /** Null when the backend does not give a renewal date. */
    val renewsAt: Instant?,
) {
    init {
        require(accountId.isNotBlank()) { "account id is required" }
    }

    fun isOn(plan: Plan): Boolean = this.plan.code == plan.code

    /** What the account would gain and lose by moving to [target]. */
    fun impactOfChangingTo(target: Plan): PlanChangeImpact =
        PlanChangeImpact(
            gained = target.capabilities - plan.capabilities,
            lost = plan.capabilities - target.capabilities,
        )
}
