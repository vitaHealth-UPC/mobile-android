package com.vitahealth.tata.carelink.infrastructure.remote

/** Older-adult profile returned by the care-link profile endpoint. */
data class OlderAdultProfileResponse(
    val id: String,
    val registeredByCaregiverId: String,
    val fullName: String,
    val birthDate: String,
    val emergencyContactName: String?,
    val emergencyContactRelationship: String?,
    val emergencyContactPhone: String?,
    val createdAt: String?,
)
