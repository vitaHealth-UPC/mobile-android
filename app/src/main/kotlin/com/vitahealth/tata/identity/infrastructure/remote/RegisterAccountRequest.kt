package com.vitahealth.tata.identity.infrastructure.remote

/** Caregiver account registration payload. */
data class RegisterAccountRequest(val name: String, val email: String, val password: String)
