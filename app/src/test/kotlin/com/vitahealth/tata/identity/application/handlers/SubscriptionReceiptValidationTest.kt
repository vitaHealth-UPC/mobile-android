package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.FakeSubscriptionRepository
import com.vitahealth.tata.identity.application.essentialPlan
import com.vitahealth.tata.identity.application.familyPlan
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommand
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** A successful transport reply must still belong to the account and plan requested. */
class SubscriptionReceiptValidationTest {
    @Test
    fun foreignAccountReadNeverBecomesTheDisplayedSubscription() = runBlocking {
        val backing = FakeSubscriptionRepository()
        val remote = object : SubscriptionRepository by backing {
            override suspend fun currentSubscription(accountId: String) =
                AppResult.Success(
                    Subscription("another-account", familyPlan, SubscriptionStatus.ACTIVE, null)
                )
        }

        val result = GetCurrentSubscriptionQueryHandler(remote)(
            GetCurrentSubscriptionQuery("account-1")
        )

        assertEquals("INVALID_SUBSCRIPTION_RESPONSE", (result as AppResult.Failure).code)
    }

    @Test
    fun changedPlanReceiptWithDifferentAccountOrPlanIsRejected() = runBlocking {
        val backing = FakeSubscriptionRepository()
        val responsePlans = listOf(
            Subscription("another-account", essentialPlan, SubscriptionStatus.ACTIVE, null),
            Subscription("account-1", familyPlan, SubscriptionStatus.ACTIVE, null),
        )
        responsePlans.forEach { receipt ->
            val remote = object : SubscriptionRepository by backing {
                override suspend fun changeSubscription(accountId: String, planCode: String) =
                    AppResult.Success(receipt)
            }

            val result = ChangeSubscriptionCommandHandler(remote)(
                ChangeSubscriptionCommand("account-1", "ESSENTIAL")
            )

            assertEquals("INVALID_SUBSCRIPTION_RESPONSE", (result as AppResult.Failure).code)
        }
    }
}
