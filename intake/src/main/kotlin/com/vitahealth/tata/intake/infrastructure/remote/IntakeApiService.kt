package com.vitahealth.tata.intake.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
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
    val confirmedAt: String? = null,
    val confirmationChannel: String? = null,
)

data class ConfirmIntakeRequest(val channel: String)

interface IntakeApiService {
    @POST("api/v1/intakes/{intakeId}/confirmation")
    suspend fun confirmDose(
        @Path("intakeId") intakeId: String,
        @Body request: ConfirmIntakeRequest,
    ): Response<IntakeResponse>

    @GET("api/v1/older-adults/{olderAdultId}/intakes/next")
    suspend fun getNextDose(
        @Path("olderAdultId") olderAdultId: String,
    ): Response<IntakeResponse>

    @GET("api/v1/intakes/{intakeId}")
    suspend fun getDoseDetail(
        @Path("intakeId") intakeId: String,
    ): Response<IntakeResponse>
}
