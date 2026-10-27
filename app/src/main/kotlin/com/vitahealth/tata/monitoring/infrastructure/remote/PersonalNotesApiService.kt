package com.vitahealth.tata.monitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Endpoints scoped to personal notes of the authenticated adult. */
interface PersonalNotesApiService {
    @GET("api/v1/me/notes") suspend fun list(): Response<List<PersonalNoteResponse>>

    @POST("api/v1/me/notes")
    suspend fun create(@Body request: CreatePersonalNoteRequest): Response<PersonalNoteResponse>
}
