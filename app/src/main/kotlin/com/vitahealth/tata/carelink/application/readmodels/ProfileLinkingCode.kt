package com.vitahealth.tata.carelink.application.readmodels

/** Temporary code and expiration presented for a registered adult. */
data class ProfileLinkingCode(
    val olderAdultId: String,
    val olderAdultName: String,
    val code: String,
    val expiresAt: String,
)
