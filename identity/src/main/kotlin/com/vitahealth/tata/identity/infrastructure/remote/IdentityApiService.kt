package com.vitahealth.tata.identity.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterAccountRequest(val name: String, val email: String, val password: String)
data class VerifyEmailRequest(val email: String, val code: String)
data class AccountResponse(val id: String, val name: String, val email: String, val status: String)

interface IdentityApiService {
    @POST("api/v1/accounts")
    suspend fun register(@Body request: RegisterAccountRequest): Response<AccountResponse>

    @POST("api/v1/accounts/verification")
    suspend fun verify(@Body request: VerifyEmailRequest): Response<AccountResponse>
}
