package com.vitahealth.tata.identity.domain.model

import com.vitahealth.tata.identity.domain.model.entities.CaregiverAccount
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.AccountStatus
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
/** Guards identity and subscription invariants, including Kotlin data-class copies. */
class IdentityDomainInvariantsTest {

    @Test
    fun caregiverAccountRejectsMissingIdentityAndName() {
        val account = CaregiverAccount("caregiver-1", "Ana", "ana@example.com", AccountStatus.PENDING_VERIFICATION)

        assertThrows(IllegalArgumentException::class.java) {
            account.copy(id = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            account.copy(name = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            account.copy(email = " ")
        }
        assertEquals("caregiver-1", account.id)
    }

    @Test
    fun planRejectsNegativePricesAndMissingReferences() {
        val plan = Plan("FAMILY", "Familiar", BigDecimal("20.00"), "PEN", emptySet())

        assertThrows(IllegalArgumentException::class.java) {
            plan.copy(code = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            plan.copy(monthlyPrice = BigDecimal("-1.00"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            plan.copy(currency = " ")
        }
        assertEquals("PEN", plan.currency)
    }

    @Test
    fun subscriptionRejectsUnknownAccountReferences() {
        val plan = Plan("BASE", "Basico", BigDecimal.ZERO, "PEN", emptySet())
        val subscription = Subscription("caregiver-1", plan, SubscriptionStatus.ACTIVE, null)

        assertThrows(IllegalArgumentException::class.java) {
            subscription.copy(accountId = " ")
        }
        assertEquals("caregiver-1", subscription.accountId)
    }
}
