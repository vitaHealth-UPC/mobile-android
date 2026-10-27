package com.vitahealth.tata.carelink.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit endpoints for adult registration and caregiver profile selection. */
interface CaregiverProfilesApiService {
    @GET("api/v1/care-links")
    suspend fun list(
        @Query("caregiverId") caregiverId: String
    ): Response<List<ConfirmedLinkResponse>>

    @POST("api/v1/older-adults")
    suspend fun register(@Body request: RegisterAdultRequest): Response<OlderAdultProfileResponse>

    @POST("api/v1/care-links/linking-codes")
    suspend fun code(@Body request: GenerateCodeRequest): Response<CareLinkResponse>
}
