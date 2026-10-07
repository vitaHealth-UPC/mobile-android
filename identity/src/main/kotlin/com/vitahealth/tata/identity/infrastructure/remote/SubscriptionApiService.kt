package com.vitahealth.tata.identity.infrastructure.remote

import java.math.BigDecimal
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class PlanDto(
    val code: String?,
    val name: String?,
    val monthlyPrice: BigDecimal?,
    val currency: String?,
    val capabilities: List<String>?,
)

data class SubscriptionDto(
    val accountId: String?,
    val plan: PlanDto?,
    val status: String?,
    val renewsAt: String?,
)

interface SubscriptionApiService {
    @GET("api/v1/plans")
    suspend fun listPlans(): Response<List<PlanDto>>

    @GET("api/v1/accounts/{accountId}/subscription")
    suspend fun currentSubscription(
        @Path("accountId") accountId: String,
    ): Response<SubscriptionDto>
}
