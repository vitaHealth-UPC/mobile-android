package com.vitahealth.tata.identity.infrastructure.remote

/** Account state and optional session issued by registration or verification. */
data class AccountResponse(
    val id: String,
    val name: String,
    val email: String,
    val status: String,
    val accessToken: String? = null,
    val expiresAt: String? = null,
)
