package com.vitahealth.tata.identity.application.readmodels

/**
 * Authenticated identity returned by session access; server authorization remains authoritative.
 */
data class SessionSubject(val subjectId: String, val role: String)
