package com.vitahealth.tata.intake.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class IntakeResponse(
    val id: String,
    val treatmentId: String,
    val medicationId: String,
    val olderAdultId: String,
    val medicationName: String,
    val dose: String,
    val instructions: String?,
    val scheduledAt: String,
    val status: String,
)

interface IntakeApiService {
    @GET("api/v1/older-adults/{olderAdultId}/intakes/next")
    suspend fun getNextDose(
        @Path("olderAdultId") olderAdultId: String,
    ): Response<IntakeResponse>
}
