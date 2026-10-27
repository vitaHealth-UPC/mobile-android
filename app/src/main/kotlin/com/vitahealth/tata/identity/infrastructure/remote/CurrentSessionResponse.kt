package com.vitahealth.tata.identity.infrastructure.remote

/** Authenticated subject reported by the current-session endpoint. */
data class CurrentSessionResponse(val subjectId: String, val role: String)
