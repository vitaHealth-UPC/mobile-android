package com.vitahealth.tata.treatment.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit endpoints for medication management and treatment lifecycle operations. */
interface TreatmentApiService {
    @GET("api/v1/me/medications")
    suspend fun getMyMedications(): Response<List<MyMedicationResponse>>

    @POST("api/v1/older-adults/{olderAdultId}/medications")
    suspend fun registerMedication(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: RegisterMedicationRequest,
    ): Response<MedicationResponse>

    @GET("api/v1/older-adults/{olderAdultId}/medications")
    suspend fun listMedications(
        @Path("olderAdultId") olderAdultId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<List<MedicationResponse>>

    @PUT("api/v1/medications/{medicationId}")
    suspend fun updateMedication(
        @Path("medicationId") medicationId: String,
        @Body request: UpdateMedicationRequest,
    ): Response<MedicationResponse>

    @POST("api/v1/medications/{medicationId}/deactivation")
    suspend fun deactivateMedication(
        @Path("medicationId") medicationId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<MedicationResponse>

    @POST("api/v1/medications/{medicationId}/reactivation")
    suspend fun reactivateMedication(
        @Path("medicationId") medicationId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<MedicationResponse>

    @POST("api/v1/older-adults/{olderAdultId}/treatments")
    suspend fun createTreatment(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: CreateTreatmentRequest,
    ): Response<TreatmentResponse>

    @GET("api/v1/older-adults/{olderAdultId}/treatments")
    suspend fun listTreatments(
        @Path("olderAdultId") olderAdultId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<List<TreatmentResponse>>

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
