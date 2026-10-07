package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.*

data class AdherenceResponse(val confirmedIntakes: Int, val totalIntakes: Int)
data class AlertResponse(val id: Long, val medicationName: String, val reason: String, val scheduledAt: String)
data class StatusResponse(val weeklyAdherence: AdherenceResponse, val openAlerts: List<AlertResponse>)
data class DoseResponse(val id: String, val medicationId: String, val medicationName: String, val dose: String,
    val scheduledAt: String, val status: String)
data class InventoryResponse(val medicationId: String, val remainingStock: Int, val lowStock: Boolean)
data class ContactResponse(val type: String, val value: String)
data class HistoryResponse(val medicationName: String, val scheduledAt: String, val status: String)
data class NoteResponse(val text: String, val recordedAt: String, val familiarId: String)

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
    @GET("api/v1/older-adults/{id}/intakes")
    suspend fun history(@Path("id") id: String, @Query("caregiverId") caregiver: String,
        @Query("days") days: Int = 7): Response<List<HistoryResponse>>
    @GET("api/v1/older-adults/{id}/notes")
    suspend fun notes(@Path("id") id: String, @Query("caregiverId") caregiver: String): Response<List<NoteResponse>>
}
