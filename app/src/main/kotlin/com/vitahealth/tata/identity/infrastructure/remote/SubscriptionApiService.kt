package com.vitahealth.tata.identity.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
/** Retrofit endpoints for the plan catalog and account subscription. */
interface SubscriptionApiService {
    @GET("api/v1/plans") suspend fun listPlans(): Response<List<PlanDto>>

    @GET("api/v1/accounts/{accountId}/subscription")
    suspend fun currentSubscription(@Path("accountId") accountId: String): Response<SubscriptionDto>

    @PUT("api/v1/accounts/{accountId}/subscription")
    suspend fun changeSubscription(
        @Path("accountId") accountId: String,
        @Body request: ChangeSubscriptionRequest,
    ): Response<SubscriptionDto>
}
