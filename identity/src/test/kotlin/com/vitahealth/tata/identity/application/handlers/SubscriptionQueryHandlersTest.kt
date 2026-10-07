package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.familyPlan
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionQueryHandlersTest {
    private val repository = FakeSubscriptionRepository()

    @Test
    fun aBlankAccountIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = GetCurrentSubscriptionQueryHandler(repository)(GetCurrentSubscriptionQuery("  "))

        assertEquals("INVALID_ACCOUNT_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.subscriptionCalls.isEmpty())
    }

    @Test
    fun theAccountIdIsTrimmedAndTheCurrentPlanIsReturned() = runBlocking {
        val result = GetCurrentSubscriptionQueryHandler(repository)(GetCurrentSubscriptionQuery(" account-1 "))

        val subscription = (result as AppResult.Success).value
        assertEquals("account-1", subscription.accountId)
        assertEquals(familyPlan, subscription.plan)
        assertEquals(listOf("account-1"), repository.subscriptionCalls)
    }

    @Test
    fun listingReturnsEveryAvailablePlan() = runBlocking {
        val result = ListAvailablePlansQueryHandler(repository)()

        assertEquals(listOf("ESSENTIAL", "FAMILY"), (result as AppResult.Success).value.map { it.code })
    }
}
