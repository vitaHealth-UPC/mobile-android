package com.vitahealth.tata.identity.domain.model.valueobjects

/** Capabilities gained and lost when comparing the current plan with a selected plan. */
data class PlanChangeImpact(val gained: Set<PlanCapability>, val lost: Set<PlanCapability>)
