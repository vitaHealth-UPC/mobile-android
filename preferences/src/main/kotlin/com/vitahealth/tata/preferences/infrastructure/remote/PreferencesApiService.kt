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

/** Body of every on/off preference endpoint. */
data class EnabledRequest(
    val enabled: Boolean,
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

    @PUT("api/v1/users/{userId}/preferences/contrast")
    suspend fun updateHighContrast(
        @Path("userId") userId: String,
        @Body request: EnabledRequest,
    ): Response<UserPreferencesResponse>

    @PUT("api/v1/users/{userId}/preferences/reduced-motion")
    suspend fun updateReducedMotion(
        @Path("userId") userId: String,
        @Body request: EnabledRequest,
    ): Response<UserPreferencesResponse>

    @PUT("api/v1/users/{userId}/preferences/voice-confirmation")
    suspend fun updateVoiceConfirmation(
        @Path("userId") userId: String,
        @Body request: EnabledRequest,
    ): Response<UserPreferencesResponse>

    @PUT("api/v1/users/{userId}/preferences/reading-assistance")
    suspend fun updateReadingAssistance(
        @Path("userId") userId: String,
        @Body request: EnabledRequest,
    ): Response<UserPreferencesResponse>
}
