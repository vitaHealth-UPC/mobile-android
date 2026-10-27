package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** HTTP contract for caregiver follow-up notes. */
interface NotesApiService {
    @GET("api/v1/older-adults/{olderAdultId}/notes")
    suspend fun notes(
        @Path("olderAdultId") olderAdultId: String,
        @Query("caregiverId") caregiverId: String,
    ): Response<List<CaregiverNoteResponse>>

    @POST("api/v1/older-adults/{olderAdultId}/notes")
    suspend fun register(
        @Path("olderAdultId") olderAdultId: String,
        @Body request: CreateCaregiverNoteRequest,
    ): Response<CaregiverNoteResponse>
}
