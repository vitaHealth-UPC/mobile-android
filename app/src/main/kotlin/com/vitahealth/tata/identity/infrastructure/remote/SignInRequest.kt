package com.vitahealth.tata.identity.infrastructure.remote

/** Password credentials sent to the caregiver session endpoint. */
data class SignInRequest(val email: String, val password: String)
