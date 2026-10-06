package com.vitahealth.tata.treatment.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
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
}
