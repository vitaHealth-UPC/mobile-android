package com.vitahealth.tata.intake.infrastructure.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class VoiceConfirmationResponse(val status: String?, val intake: IntakeResponse? = null)

interface VoiceConfirmationApiService {
    @Multipart
    @POST("api/v1/intakes/{intakeId}/voice-confirmation")
    suspend fun confirm(@Path("intakeId") intakeId: String, @Part audio: MultipartBody.Part, @Query("language") language: String): Response<VoiceConfirmationResponse>
}
