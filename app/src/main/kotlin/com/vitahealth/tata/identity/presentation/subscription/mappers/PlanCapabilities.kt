package com.vitahealth.tata.identity.presentation.subscription.mappers

import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability

private val coreCapabilities =
    setOf(PlanCapability.REMINDERS, PlanCapability.AGENDA, PlanCapability.INTAKE_CONFIRMATION)

/** Highlights family additions only when the received plan also includes the core capabilities. */
internal fun featuredPlanCapabilities(capabilities: Set<PlanCapability>): List<PlanCapability> {
    val additions = capabilities - coreCapabilities
    val featured =
        if (capabilities.containsAll(coreCapabilities) && additions.isNotEmpty()) additions
        else capabilities
    return featured.sortedBy { it.ordinal }
}
