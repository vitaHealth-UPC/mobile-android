package com.vitahealth.tata.identity.domain.model

import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.identity.application.familyPlan
import com.vitahealth.tata.identity.application.renewalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTest {
    private val subscription = Subscription("account-1", familyPlan, SubscriptionStatus.ACTIVE, renewalDate)

    @Test
    fun theCurrentPlanIsRecognisedByItsCode() {
        assertTrue(subscription.isOn(familyPlan))
        assertTrue(subscription.isOn(familyPlan.copy(name = "Familiar renamed")))
        assertFalse(subscription.isOn(essentialPlan))
    }

    @Test
    fun aPlanSupportsOnlyItsCapabilities() {
        assertTrue(essentialPlan.supports(PlanCapability.REMINDERS))
        assertFalse(essentialPlan.supports(PlanCapability.FAMILY_ALERTS))
        assertTrue(familyPlan.supports(PlanCapability.ADHERENCE_INSIGHTS))
    }

    @Test
    fun movingToAPlanWithFewerCapabilitiesLosesTheDifference() {
        val impact = subscription.impactOfChangingTo(essentialPlan)

        assertTrue(impact.gained.isEmpty())
        assertEquals(
            setOf(PlanCapability.FAMILY_ALERTS, PlanCapability.FAMILY_MONITORING, PlanCapability.ADHERENCE_INSIGHTS),
            impact.lost,
        )
    }

    @Test
    fun movingToAPlanWithMoreCapabilitiesGainsTheDifference() {
        val onEssential = Subscription("account-1", essentialPlan, SubscriptionStatus.ACTIVE, renewalDate)

        val impact = onEssential.impactOfChangingTo(familyPlan)

        assertTrue(impact.lost.isEmpty())
        assertEquals(
            setOf(PlanCapability.FAMILY_ALERTS, PlanCapability.FAMILY_MONITORING, PlanCapability.ADHERENCE_INSIGHTS),
            impact.gained,
        )
    }

    @Test
    fun stayingOnTheSamePlanChangesNothing() {
        val impact = subscription.impactOfChangingTo(familyPlan)

        assertTrue(impact.gained.isEmpty())
        assertTrue(impact.lost.isEmpty())
    }
}
