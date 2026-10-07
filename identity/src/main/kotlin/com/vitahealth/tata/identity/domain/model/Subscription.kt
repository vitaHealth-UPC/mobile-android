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
}
