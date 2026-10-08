package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.domain.model.PlanCapability
import com.vitahealth.tata.identity.domain.model.SubscriptionStatus
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubscriptionMapperTest {
    private fun plan(
        code: String? = "FAMILY",
        name: String? = "Familiar",
        price: BigDecimal? = BigDecimal("19.90"),
        currency: String? = "PEN",
        capabilities: List<String>? = listOf("REMINDERS", "FAMILY_ALERTS"),
    ) = PlanDto(code, name, price, currency, capabilities)

    private fun subscription(
        plan: PlanDto? = plan(),
        status: String? = "ACTIVE",
        renewsAt: String? = "2026-10-28T00:00:00Z",
        accountId: String? = "account-1",
    ) = SubscriptionDto(accountId, plan, status, renewsAt)

    @Test
    fun aFullPlanIsMapped() {
        val mapped = plan().toDomain()!!

        assertEquals("FAMILY", mapped.code)
        assertEquals("Familiar", mapped.name)
        assertEquals(BigDecimal("19.90"), mapped.monthlyPrice)
        assertEquals("PEN", mapped.currency)
        assertEquals(setOf(PlanCapability.REMINDERS, PlanCapability.FAMILY_ALERTS), mapped.capabilities)
    }

    @Test
    fun aCapabilityThisAppDoesNotKnowIsSkippedInsteadOfBreakingTheScreen() {
        val mapped = plan(capabilities = listOf("REMINDERS", "TELEMEDICINE")).toDomain()!!

        assertEquals(setOf(PlanCapability.REMINDERS), mapped.capabilities)
    }

    @Test
    fun aPlanWithoutCapabilitiesHasNone() {
        assertEquals(emptySet<PlanCapability>(), plan(capabilities = null).toDomain()!!.capabilities)
    }

    @Test
    fun aPlanMissingAnEssentialFieldIsRejected() {
        assertNull(plan(code = " ").toDomain())
        assertNull(plan(name = null).toDomain())
        assertNull(plan(price = null).toDomain())
        assertNull(plan(currency = "").toDomain())
    }

    @Test
    fun aSubscriptionIsMappedWithItsRenewalDate() {
        val mapped = subscription().toDomain()!!

        assertEquals("account-1", mapped.accountId)
        assertEquals(SubscriptionStatus.ACTIVE, mapped.status)
        assertEquals(Instant.parse("2026-10-28T00:00:00Z"), mapped.renewsAt)
    }

    @Test
    fun aMissingOrBrokenRenewalDateIsKeptAsNull() {
        assertNull(subscription(renewsAt = null).toDomain()!!.renewsAt)
        assertNull(subscription(renewsAt = "28 oct").toDomain()!!.renewsAt)
    }

    @Test
    fun aSubscriptionWithAnUnknownStatusOrNoPlanIsRejected() {
        assertNull(subscription(status = "SUSPENDED").toDomain())
        assertNull(subscription(plan = null).toDomain())
        assertNull(subscription(accountId = null).toDomain())
    }
}
