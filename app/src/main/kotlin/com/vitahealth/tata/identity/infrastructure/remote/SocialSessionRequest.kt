package com.vitahealth.tata.identity.infrastructure.remote

/** Firebase token is a bearer credential, not a client-supplied account or email identity. */
data class SocialSessionRequest(val firebaseIdToken: String) {
    override fun toString(): String = "SocialSessionRequest[firebaseIdToken=REDACTED]"
}
