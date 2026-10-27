package com.vitahealth.tata.identity.infrastructure.remote

/** Session credentials and the subject identity returned by authentication. */
data class SessionAccessResponse(
    val accountId: String?,
    val olderAdultId: String?,
    val accessToken: String,
    val expiresAt: String,
)
