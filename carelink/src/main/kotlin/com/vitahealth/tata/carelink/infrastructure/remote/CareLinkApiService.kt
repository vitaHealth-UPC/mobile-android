package com.vitahealth.tata.carelink.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class AcceptCareLinkRequest(
    val caregiverId: String,
    val code: String,
)

data class RegisterConsentRequest(
    val accepted: Boolean,
)

data class CareLinkResponse(
    val id: String,
    val caregiverId: String,
    val olderAdultId: String,
    val status: String,
    val linkingCode: String?,
    val codeExpiresAt: String?,
    val codeUsedAt: String?,
    val consentGranted: Boolean,
    val consentRecordedAt: String?,
    val confirmedAt: String?,
)

data class OlderAdultProfileResponse(
    val id: String,
    val registeredByCaregiverId: String,
    val fullName: String,
    val birthDate: String,
    val emergencyContactName: String?,
    val emergencyContactRelationship: String?,
    val emergencyContactPhone: String?,
    val createdAt: String?,
)

interface CareLinkApiService {
    @POST("api/v1/care-links/acceptances")
    suspend fun acceptLink(
        @Body request: AcceptCareLinkRequest,
    ): Response<CareLinkResponse>

    @POST("api/v1/care-links/{careLinkId}/consent")
    suspend fun registerConsent(
        @Path("careLinkId") careLinkId: String,
        @Body request: RegisterConsentRequest,
    ): Response<CareLinkResponse>

    @GET("api/v1/older-adults/{olderAdultId}")
    suspend fun getOlderAdult(
        @Path("olderAdultId") olderAdultId: String,
    ): Response<OlderAdultProfileResponse>
}
