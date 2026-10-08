package com.vitahealth.tata.carelink.infrastructure.remote
import retrofit2.Response
import retrofit2.http.*

data class RegisterAdultRequest(val caregiverId: String,val fullName: String,val birthDate: String,val emergencyContactName: String?,val emergencyContactRelationship: String?,val emergencyContactPhone: String?)
data class GenerateCodeRequest(val caregiverId: String,val olderAdultId: String)
data class ConfirmedLinkResponse(val id: String,val olderAdultId: String,val olderAdultName: String)
interface CaregiverProfilesApiService {
    @GET("api/v1/care-links") suspend fun list(@Query("caregiverId") caregiverId: String): Response<List<ConfirmedLinkResponse>>
    @POST("api/v1/older-adults") suspend fun register(@Body request: RegisterAdultRequest): Response<OlderAdultProfileResponse>
    @POST("api/v1/care-links/linking-codes") suspend fun code(@Body request: GenerateCodeRequest): Response<CareLinkResponse>
}
