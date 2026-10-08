package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.*

data class AdherenceResponse(val confirmedIntakes: Int, val totalIntakes: Int)
data class StatusResponse(val weeklyAdherence: AdherenceResponse, val openAlerts: List<AlertSummaryResponse>?)
data class DoseResponse(val id: String, val medicationId: String, val medicationName: String, val dose: String,
    val scheduledAt: String, val status: String)
data class InventoryResponse(val medicationId: String, val remainingStock: Int, val lowStock: Boolean)
/** `ContactChannelResource`: type PHONE or WHATSAPP. */
data class ContactResponse(val type: String?, val value: String?)
/** Only the name of `OlderAdultProfileResource` is read. */
data class OlderAdultProfileResponse(val id: String?, val fullName: String?)
data class HistoryResponse(val medicationName: String, val scheduledAt: String, val status: String)

interface FamilyMonitoringApiService {
    @GET("api/v1/older-adults/{id}/status")
    suspend fun status(@Path("id") id: String, @Query("caregiverId") caregiver: String): Response<StatusResponse>
    @GET("api/v1/older-adults/{id}/intakes/agenda")
    suspend fun agenda(@Path("id") id: String, @Query("from") from: String, @Query("to") to: String): Response<List<DoseResponse>>
    @GET("api/v1/older-adults/{id}/intakes/next")
    suspend fun next(@Path("id") id: String): Response<DoseResponse>
    @GET("api/v1/inventories/{medicationId}")
    suspend fun inventory(@Path("medicationId") id: String): Response<InventoryResponse>
    @GET("api/v1/older-adults/{id}/contact-channel")
    suspend fun contact(@Path("id") id: String, @Query("caregiverId") caregiver: String): Response<ContactResponse>
    @GET("api/v1/older-adults/{id}")
    suspend fun profile(@Path("id") id: String): Response<OlderAdultProfileResponse>
    @GET("api/v1/older-adults/{id}/intakes")
    suspend fun history(@Path("id") id: String, @Query("caregiverId") caregiver: String,
        @Query("days") days: Int = 7): Response<List<HistoryResponse>>
}
