package com.vitahealth.tata.identity.infrastructure.remote

/** Request to issue another email verification challenge. */
data class CreateEmailVerificationRequest(val email: String)
