package com.vitahealth.tata.inventory.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Body of US-40 "register initial inventory". */
data class RegisterInitialInventoryRequest(
    val medicationId: String,
    val initialQuantity: Int,
    val replenishmentThreshold: Int,
)

/** Body of US-43 "register replenishment". Optional lot metadata is supported. */
data class RegisterReplenishmentRequest(
    val quantity: Int,
    val lot: String? = null,
)

/**
 * Mirrors the backend `InventoryResource`. Timestamps are ISO-8601 strings because the shared
 * Retrofit uses the default Gson, which does not deserialize `java.time.Instant`; they are
 * parsed in the repository. Coverage is computed by the backend.
 */
data class InventoryResponse(
    val id: String,
    val medicationId: String,
    val remainingStock: Int,
    val replenishmentThreshold: Int,
    val lowStock: Boolean,
    val batches: List<BatchResponse>,
    val createdAt: String,
    val updatedAt: String,
    val daysRemaining: Int? = null,
    val dailyConsumptionUnits: Int? = null,
)

/** Mirrors the backend `BatchResource`: quantity, timestamp and optional lot metadata. */
data class BatchResponse(
    val id: String,
    val quantity: Int,
    val registeredAt: String,
    val lot: String? = null,
)

interface InventoryApiService {
    @POST("api/v1/inventories")
    suspend fun registerInitialInventory(
        @Body request: RegisterInitialInventoryRequest,
    ): Response<InventoryResponse>

    @GET("api/v1/inventories/{medicationId}")
    suspend fun getStock(
        @Path("medicationId") medicationId: String,
    ): Response<InventoryResponse>

    @POST("api/v1/inventories/{medicationId}/replenishments")
    suspend fun registerReplenishment(
        @Path("medicationId") medicationId: String,
        @Body request: RegisterReplenishmentRequest,
    ): Response<InventoryResponse>
}
