package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * `AlertSummaryResource` of the backend. Every field is nullable because Gson does not enforce
 * Kotlin nullability; [toDomain] validates the payload before it reaches the domain.
 */
data class AlertSummaryResponse(
    val id: Long?,
    val intakeId: String?,
    val medicationName: String?,
    val scheduledAt: String?,
    val reason: String?,
    val status: String?,
    val openedAt: String?,
    val closedAt: String?,
)

/** `UpdateAlertStatusResource`: only ATTENDED and CLOSED are accepted. */
data class UpdateAlertStatusRequest(val status: String)

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
