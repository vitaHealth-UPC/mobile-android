package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

/** Guards cancellation propagation and the distinction between transport and invalid payloads. */
class SubscriptionFailureClassificationTest {
    private fun repository(error: Exception) =
        RemoteSubscriptionRepository(
            object : SubscriptionApiService {
                override suspend fun listPlans(): Response<List<PlanDto>> = throw error

                override suspend fun currentSubscription(
                    accountId: String
                ): Response<SubscriptionDto> = throw error

                override suspend fun changeSubscription(
                    accountId: String,
                    request: ChangeSubscriptionRequest,
                ): Response<SubscriptionDto> = throw error
            }
        )

    @Test
    fun cancellationPropagatesToTheCaller() = runBlocking {
        val cancellation = CancellationException("screen closed")
        try {
            repository(cancellation).listPlans()
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    @Test
    fun transportFailureIsNotAnInvalidPayload() = runBlocking {
        val result = repository(IOException("offline")).listPlans() as AppResult.Failure
        assertEquals("NETWORK_UNAVAILABLE", result.code)
    }

    @Test
    fun invalidPayloadIsNotReportedAsOffline() = runBlocking {
        val result =
            repository(IllegalArgumentException("bad payload")).listPlans() as AppResult.Failure
        assertEquals("INVALID_SUBSCRIPTION_RESPONSE", result.code)
    }

    @Test
    fun subscriptionResponsesMustBelongToTheRequestedAccount() = runBlocking {
        val body =
            SubscriptionDto(
                "different-account",
                PlanDto("ESSENTIAL", "Essential", java.math.BigDecimal.ZERO, "PEN", emptyList()),
                "ACTIVE",
                null,
            )
        val api =
            object : SubscriptionApiService {
                override suspend fun listPlans(): Response<List<PlanDto>> =
                    Response.success(emptyList())

                override suspend fun currentSubscription(accountId: String) = Response.success(body)

                override suspend fun changeSubscription(
                    accountId: String,
                    request: ChangeSubscriptionRequest,
                ) = Response.success(body)
            }
        val repository = RemoteSubscriptionRepository(api)
        assertEquals(
            "INVALID_SUBSCRIPTION_RESPONSE",
            (repository.currentSubscription("account-1") as AppResult.Failure).code,
        )
        assertEquals(
            "INVALID_SUBSCRIPTION_RESPONSE",
            (repository.changeSubscription("account-1", "ESSENTIAL") as AppResult.Failure).code,
        )
    }

    @Test
    fun changeReceiptMustConfirmTheRequestedPlan() = runBlocking {
        val api = responses()
        assertEquals(
            "INVALID_SUBSCRIPTION_RESPONSE",
            (RemoteSubscriptionRepository(api).changeSubscription("account-1", "FAMILY")
                    as AppResult.Failure)
                .code,
        )
        val accepted =
            RemoteSubscriptionRepository(api).changeSubscription("account-1", "ESSENTIAL")
                as AppResult.Success
        assertEquals("ESSENTIAL", accepted.value.plan.code)
    }

    @Test
    fun duplicatePlanCodesAreRejectedInsteadOfShowingAmbiguousChoices() = runBlocking {
        val plan =
            PlanDto("ESSENTIAL", "Esencial", java.math.BigDecimal("9.90"), "PEN", emptyList())
        val result =
            RemoteSubscriptionRepository(responses(listOf(plan, plan))).listPlans()
                as AppResult.Failure
        assertEquals("INVALID_SUBSCRIPTION_RESPONSE", result.code)
    }

    @Test
    fun successfulResponseWithoutBodyIsInvalidRatherThanAnAccountProblem() = runBlocking {
        val api = responses(subscription = Response.success<SubscriptionDto>(204, null))
        assertEquals(
            "INVALID_SUBSCRIPTION_RESPONSE",
            (RemoteSubscriptionRepository(api).currentSubscription("account-1")
                    as AppResult.Failure)
                .code,
        )
    }

    @Test
    fun malformedJsonIsNotReportedAsOfflineThroughTheRealHttpClient() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("[{\"code\":\"ESSENTIAL\"")
            )
            val api =
                ApiClientFactory.create(server.url("/").toString()) { null }
                    .create(SubscriptionApiService::class.java)
            assertEquals(
                "INVALID_SUBSCRIPTION_RESPONSE",
                (RemoteSubscriptionRepository(api).listPlans() as AppResult.Failure).code,
            )
            assertEquals(
                "INVALID_SUBSCRIPTION_RESPONSE",
                (repository(MalformedJsonException("broken")).listPlans() as AppResult.Failure).code,
            )
        } finally {
            server.shutdown()
        }
    }

    private fun responses(
        plans: List<PlanDto> = emptyList(),
        subscription: Response<SubscriptionDto> =
            Response.success(
                SubscriptionDto(
                    "account-1",
                    PlanDto(
                        "ESSENTIAL",
                        "Esencial",
                        java.math.BigDecimal("9.90"),
                        "PEN",
                        emptyList(),
                    ),
                    "ACTIVE",
                    null,
                )
            ),
    ) =
        object : SubscriptionApiService {
            override suspend fun listPlans() = Response.success(plans)

            override suspend fun currentSubscription(accountId: String) = subscription

            override suspend fun changeSubscription(
                accountId: String,
                request: ChangeSubscriptionRequest,
            ) = subscription
        }
}
