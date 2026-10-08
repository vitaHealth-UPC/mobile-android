package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommand
import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeSubscriptionCommandHandlerTest {
    private val repository = FakeSubscriptionRepository()
    private val handler = ChangeSubscriptionCommandHandler(repository)

    @Test
    fun aBlankAccountIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(ChangeSubscriptionCommand(accountId = " ", planCode = "ESSENTIAL"))

        assertEquals("INVALID_ACCOUNT_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.changes.isEmpty())
    }

    @Test
    fun aBlankPlanIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(ChangeSubscriptionCommand(accountId = "account-1", planCode = ""))

        assertEquals("INVALID_PLAN_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.changes.isEmpty())
    }

    @Test
    fun theValuesAreTrimmedAndTheNewPlanIsReturned() = runBlocking {
        val result = handler(ChangeSubscriptionCommand(" account-1 ", " ESSENTIAL "))

        assertEquals(essentialPlan, (result as AppResult.Success).value.plan)
        assertEquals(listOf("account-1" to "ESSENTIAL"), repository.changes)
    }

    @Test
    fun anUnknownPlanIsReportedByTheBackend() = runBlocking {
        val result = handler(ChangeSubscriptionCommand("account-1", "GOLD"))

        assertEquals("PLAN_NOT_FOUND", (result as AppResult.Failure).code)
    }
}
