package com.vitahealth.tata.treatment.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path

data class RegisterMedicationRequest(
    val caregiverId: String,
    val name: String,
    val presentation: String,
)

data class MedicationResponse(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
    val active: Boolean,
)

data class CreateTreatmentRequest(
    val caregiverId: String,
    val name: String,
)

data class ConfigureTreatmentRequest(
    val caregiverId: String,
    val medicationId: String,
    val dose: String,
    val frequency: String,
    val scheduledTimes: List<String>,
    val instructions: String,
    val reminderLeadMinutes: Int,
)

data class TreatmentResponse(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val status: String,
    val medicationId: String?,
    val dose: String?,
    val frequency: String?,
    val scheduledTimes: List<String>?,
    val instructions: String?,
    val reminderLeadMinutes: Int?,
)

interface TreatmentApiService {
    @POST("api/v1/older-adults/{olderAdultId}/medications")
    suspend fun registerMedication(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: RegisterMedicationRequest,
    ): Response<MedicationResponse>

    @POST("api/v1/older-adults/{olderAdultId}/treatments")
    suspend fun createTreatment(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: CreateTreatmentRequest,
    ): Response<TreatmentResponse>

    @PUT("api/v1/treatments/{treatmentId}/regimen")
    suspend fun configureTreatment(
        @Path("treatmentId") treatmentId: String,
        @Body request: ConfigureTreatmentRequest,
    ): Response<TreatmentResponse>

    @POST("api/v1/treatments/{treatmentId}/activation")
    suspend fun activateTreatment(
        @Path("treatmentId") treatmentId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<TreatmentResponse>

    @POST("api/v1/treatments/{treatmentId}/pause")
    suspend fun pauseTreatment(
        @Path("treatmentId") treatmentId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<TreatmentResponse>

    @POST("api/v1/treatments/{treatmentId}/resume")
    suspend fun resumeTreatment(
        @Path("treatmentId") treatmentId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<TreatmentResponse>

    @GET("api/v1/treatments/{treatmentId}")
    suspend fun getTreatmentDetail(
        @Path("treatmentId") treatmentId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<TreatmentResponse>
}
