package com.vitahealth.tata.identity.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
/** Retrofit contract for password sessions, PIN access and session revocation. */
interface SessionApiService {
    @POST("api/v1/sessions")
    suspend fun signIn(@Body request: SignInRequest): Response<SessionAccessResponse>

    @POST("api/v1/social-sessions")
    suspend fun signInSocial(@Body request: SocialSessionRequest): Response<SessionAccessResponse>

    @PUT("api/v1/me/social-identities")
    suspend fun linkSocial(@Body request: SocialSessionRequest): Response<Unit>

    @POST("api/v1/pin-sessions")
    suspend fun signInWithPin(@Body request: PinAccessRequest): Response<SessionAccessResponse>

    @POST("api/v1/pin-credentials")
    suspend fun registerPin(@Body request: PinAccessRequest): Response<Unit>

    @GET("api/v1/sessions/current") suspend fun current(): Response<CurrentSessionResponse>

    @DELETE("api/v1/sessions") suspend fun signOut(): Response<Unit>
}
