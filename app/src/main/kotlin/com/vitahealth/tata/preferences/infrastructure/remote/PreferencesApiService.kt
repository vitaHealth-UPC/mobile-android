package com.vitahealth.tata.preferences.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** HTTP contract for user accessibility and notification preferences. */
interface PreferencesApiService {
    @PUT("api/v1/users/{userId}/notification-preferences/policy")
    suspend fun updateNotificationPolicy(
        @Path("userId") userId: String,
        @Body request: UpdateNotificationPolicyRequest,
    ): Response<UserPreferencesResponse>

    @GET("api/v1/users/{userId}/preferences")
    suspend fun getPreferences(@Path("userId") userId: String): Response<UserPreferencesResponse>

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

    @PUT("api/v1/users/{userId}/preferences/dark-theme")
    suspend fun updateDarkTheme(
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

    @PUT("api/v1/users/{userId}/notification-preferences")
    suspend fun updateNotificationPreferences(
        @Path("userId") userId: String,
        @Body request: UpdateNotificationPreferencesRequest,
    ): Response<UserPreferencesResponse>
}
