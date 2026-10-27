package com.vitahealth.tata.carelink.infrastructure.remote

/** Care-link state and optional setup or adult session credentials. */
data class CareLinkResponse(
    val id: String,
    val caregiverId: String,
    val olderAdultId: String,
    val status: String,
    val linkingCode: String?,
    val codeExpiresAt: String?,
    val codeUsedAt: String?,
    val consentGranted: Boolean?,
    val consentRecordedAt: String?,
    val confirmedAt: String?,
    val accessToken: String? = null,
    val expiresAt: String? = null,
)
