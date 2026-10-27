package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Read-only HTTP contract for protected family monitoring resources. */
interface FamilyMonitoringApiService {
    @GET("api/v1/older-adults/{id}/status")
    suspend fun status(
        @Path("id") id: String,
        @Query("caregiverId") caregiver: String,
    ): Response<StatusResponse>

    @GET("api/v1/older-adults/{id}/intakes/agenda")
    suspend fun agenda(
        @Path("id") id: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<List<DoseResponse>>

    @GET("api/v1/older-adults/{id}/intakes/next")
    suspend fun next(@Path("id") id: String): Response<DoseResponse>

    @GET("api/v1/older-adults/{id}/alerts/{alertId}/timeline")
    suspend fun alertTimeline(
        @Path("id") id: String,
        @Path("alertId") alertId: Long,
        @Query("caregiverId") caregiverId: String,
    ): Response<List<RecordedAlertEventResponse>>

    @GET("api/v1/intakes/{intakeId}")
    suspend fun dose(@Path("intakeId") intakeId: String): Response<DoseResponse>

    @GET("api/v1/inventories/{medicationId}")
    suspend fun inventory(@Path("medicationId") id: String): Response<InventoryResponse>

    @GET("api/v1/older-adults/{id}/contact-channel")
    suspend fun contact(
        @Path("id") id: String,
        @Query("caregiverId") caregiver: String,
    ): Response<ContactResponse>

    @GET("api/v1/older-adults/{id}")
    suspend fun profile(@Path("id") id: String): Response<OlderAdultProfileResponse>

    @GET("api/v1/older-adults/{id}/intakes")
    suspend fun history(
        @Path("id") id: String,
        @Query("caregiverId") caregiver: String,
        @Query("days") days: Int = 7,
    ): Response<List<HistoryResponse>>
}
