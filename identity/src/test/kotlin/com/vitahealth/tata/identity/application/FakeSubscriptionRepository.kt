package com.vitahealth.tata.identity.application

import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.PlanCapability
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.identity.domain.model.SubscriptionStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.math.BigDecimal
import java.time.Instant

val essentialPlan = Plan(
    code = "ESSENTIAL",
    name = "Esencial",
    monthlyPrice = BigDecimal("9.90"),
    currency = "PEN",
    capabilities = setOf(PlanCapability.REMINDERS, PlanCapability.AGENDA, PlanCapability.INTAKE_CONFIRMATION),
)

val familyPlan = Plan(
    code = "FAMILY",
    name = "Familiar",
    monthlyPrice = BigDecimal("19.90"),
    currency = "PEN",
    capabilities = PlanCapability.entries.toSet(),
)

val renewalDate: Instant = Instant.parse("2026-10-28T00:00:00Z")

val NoConnection = AppResult.Failure(message = "offline", code = "NETWORK_UNAVAILABLE")

/** A backend with two plans and one account on the family plan; it can be told to fail. */
class FakeSubscriptionRepository(
    var subscribedTo: Plan = familyPlan,
) : SubscriptionRepository {
    var failWith: AppResult.Failure? = null
    var planCalls = 0
    val subscriptionCalls = mutableListOf<String>()

    override suspend fun listPlans(): AppResult<List<Plan>> {
        planCalls++
        return failWith ?: AppResult.Success(listOf(essentialPlan, familyPlan))
    }

    override suspend fun currentSubscription(accountId: String): AppResult<Subscription> {
        subscriptionCalls += accountId
        return failWith ?: AppResult.Success(Subscription(accountId, subscribedTo, SubscriptionStatus.ACTIVE, renewalDate))
    }
}
