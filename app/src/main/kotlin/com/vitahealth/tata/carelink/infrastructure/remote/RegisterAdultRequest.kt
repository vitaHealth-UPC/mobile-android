package com.vitahealth.tata.carelink.infrastructure.remote

/** Payload for registering an adult and an optional emergency contact. */
data class RegisterAdultRequest(
    val caregiverId: String,
    val fullName: String,
    val birthDate: String,
    val emergencyContactName: String?,
    val emergencyContactRelationship: String?,
    val emergencyContactPhone: String?,
)
