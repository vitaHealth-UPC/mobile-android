package com.vitahealth.tata.monitoring.infrastructure.remote

/** Identity and display name read from the older adult profile resource. */
data class OlderAdultProfileResponse(
    val id: String?,
    val fullName: String?,
    val birthDate: String? = null,
)
