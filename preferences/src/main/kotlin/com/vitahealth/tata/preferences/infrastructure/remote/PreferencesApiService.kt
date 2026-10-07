package com.vitahealth.tata.preferences.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class QuietHoursDto(
    val start: String,
    val end: String,
)

data class ChannelDto(
    val type: String,
    val enabled: Boolean,
)

data class UserPreferencesResponse(
    val userId: String?,
    val textSize: String?,
    val highContrast: Boolean,
    val reducedMotion: Boolean,
    val readingAssistance: Boolean,
    val voiceConfirmationEnabled: Boolean,
    val quietHours: QuietHoursDto?,
    val notificationChannels: List<ChannelDto>?,
)

data class UpdateTextSizeRequest(
    val textSize: String,
)

interface PreferencesApiService {
    @GET("api/v1/users/{userId}/preferences")
    suspend fun getPreferences(
        @Path("userId") userId: String,
    ): Response<UserPreferencesResponse>

    @PUT("api/v1/users/{userId}/preferences/text-size")
    suspend fun updateTextSize(
        @Path("userId") userId: String,
        @Body request: UpdateTextSizeRequest,
    ): Response<UserPreferencesResponse>
}
