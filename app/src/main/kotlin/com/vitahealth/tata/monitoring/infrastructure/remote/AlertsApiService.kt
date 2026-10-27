package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** HTTP contract for protected alert details and status transitions. */
interface AlertsApiService {
    @PUT("api/v1/older-adults/{olderAdultId}/alerts/{alertId}/status")
    suspend fun updateStatus(
        @Path("olderAdultId") olderAdultId: String,
        @Path("alertId") alertId: Long,
        @Query("caregiverId") caregiverId: String,
        @Body request: UpdateAlertStatusRequest,
    ): Response<AlertSummaryResponse>

    @GET("api/v1/older-adults/{olderAdultId}/alerts/{alertId}")
    suspend fun detail(
        @Path("olderAdultId") olderAdultId: String,
        @Path("alertId") alertId: Long,
        @Query("caregiverId") caregiverId: String,
    ): Response<AlertSummaryResponse>
}
