package com.vitahealth.tata.identity.infrastructure.remote

/** Email verification challenge response. */
data class VerifyEmailRequest(val email: String, val code: String)
