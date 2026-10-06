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

interface TreatmentApiService {
    @POST("api/v1/older-adults/{olderAdultId}/medications")
    suspend fun registerMedication(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: RegisterMedicationRequest,
    ): Response<MedicationResponse>
}
