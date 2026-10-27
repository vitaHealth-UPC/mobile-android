package com.vitahealth.tata.intake.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Queries intake schedules and submits explicit dose confirmations. */
interface IntakeApiService {
    @GET("api/v1/older-adults/{olderAdultId}/intakes/agenda")
    suspend fun getAgenda(
        @Path("olderAdultId") olderAdultId: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<List<IntakeResponse>>

    @POST("api/v1/intakes/{intakeId}/confirmation")
    suspend fun confirmDose(
        @Path("intakeId") intakeId: String,
        @Body request: ConfirmIntakeRequest,
    ): Response<IntakeResponse>

    @GET("api/v1/older-adults/{olderAdultId}/intakes/next")
    suspend fun getNextDose(@Path("olderAdultId") olderAdultId: String): Response<IntakeResponse>

    @GET("api/v1/intakes/{intakeId}")
    suspend fun getDoseDetail(@Path("intakeId") intakeId: String): Response<IntakeResponse>
}
