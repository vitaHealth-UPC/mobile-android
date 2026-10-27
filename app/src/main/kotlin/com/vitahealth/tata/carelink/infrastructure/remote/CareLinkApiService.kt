package com.vitahealth.tata.carelink.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit contract for link acceptance, consent and older-adult profile retrieval. */
interface CareLinkApiService {
    @POST("api/v1/care-links/acceptances")
    suspend fun acceptLink(@Body request: AcceptCareLinkRequest): Response<CareLinkResponse>

    @POST("api/v1/care-links/{careLinkId}/consent")
    suspend fun registerConsent(
        @Path("careLinkId") careLinkId: String,
        @Body request: RegisterConsentRequest,
    ): Response<CareLinkResponse>

    @GET("api/v1/older-adults/{olderAdultId}")
    suspend fun getOlderAdult(
        @Path("olderAdultId") olderAdultId: String
    ): Response<OlderAdultProfileResponse>
}
