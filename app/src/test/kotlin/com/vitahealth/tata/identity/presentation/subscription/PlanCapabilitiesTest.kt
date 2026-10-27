package com.vitahealth.tata.identity.presentation.subscription

import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.identity.presentation.subscription.mappers.featuredPlanCapabilities
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanCapabilitiesTest {
    @Test
    fun partialCatalogDoesNotAdvertiseMissingCapabilities() {
        assertEquals(
            listOf(PlanCapability.REMINDERS),
            featuredPlanCapabilities(setOf(PlanCapability.REMINDERS)),
        )
        assertEquals(emptyList<PlanCapability>(), featuredPlanCapabilities(emptySet()))
    }

    @Test
    fun completeFamilyCatalogHighlightsActualAdditions() {
        assertEquals(
            listOf(
                PlanCapability.FAMILY_ALERTS,
                PlanCapability.FAMILY_MONITORING,
                PlanCapability.ADHERENCE_INSIGHTS,
            ),
            featuredPlanCapabilities(PlanCapability.entries.toSet()),
        )
        assertEquals(
            listOf(PlanCapability.FAMILY_ALERTS),
            featuredPlanCapabilities(setOf(PlanCapability.FAMILY_ALERTS)),
        )
    }
}
