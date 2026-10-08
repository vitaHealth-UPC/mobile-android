package com.vitahealth.tata.identity.infrastructure.remote

import retrofit2.Response
import retrofit2.http.*

data class SignInRequest(val email: String, val password: String)
data class PinAccessRequest(val olderAdultId: String, val pin: String)
data class SessionAccessResponse(val accountId: String?, val olderAdultId: String?, val accessToken: String, val expiresAt: String)
data class CurrentSessionResponse(val subjectId: String, val role: String)
interface SessionApiService {
    @POST("api/v1/sessions") suspend fun signIn(@Body request: SignInRequest): Response<SessionAccessResponse>
    @POST("api/v1/pin-sessions") suspend fun signInWithPin(@Body request: PinAccessRequest): Response<SessionAccessResponse>
    @POST("api/v1/pin-credentials") suspend fun registerPin(@Body request: PinAccessRequest): Response<Unit>
    @GET("api/v1/sessions/current") suspend fun current(): Response<CurrentSessionResponse>
    @DELETE("api/v1/sessions") suspend fun signOut(): Response<Unit>
}
