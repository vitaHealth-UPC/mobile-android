package com.vitahealth.tata.identity.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
/** Account registration and email verification HTTP contract. */
interface IdentityApiService {
    @POST("api/v1/accounts")
    suspend fun register(@Body request: RegisterAccountRequest): Response<AccountResponse>

    @POST("api/v1/accounts/verification")
    suspend fun verify(@Body request: VerifyEmailRequest): Response<AccountResponse>

    @POST("api/v1/email-verification-requests")
    suspend fun requestNewVerification(
        @Body request: CreateEmailVerificationRequest
    ): Response<AccountResponse>
}
