package com.vitahealth.tata.inventory.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit endpoints for medication stock initialization, queries and replenishment. */
interface InventoryApiService {
    @POST("api/v1/inventories")
    suspend fun registerInitialInventory(
        @Body request: RegisterInitialInventoryRequest
    ): Response<InventoryResponse>

    @GET("api/v1/inventories/{medicationId}")
    suspend fun getStock(@Path("medicationId") medicationId: String): Response<InventoryResponse>

    @POST("api/v1/inventories/{medicationId}/replenishments")
    suspend fun registerReplenishment(
        @Path("medicationId") medicationId: String,
        @Body request: RegisterReplenishmentRequest,
    ): Response<InventoryResponse>
}
