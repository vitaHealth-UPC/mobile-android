package com.vitahealth.tata.shared.application.session

/** Backend-validated subject used to partition local data, never to grant server authorization. */
data class SessionOwner(val subjectId: String, val role: String) {
    init {
        require(subjectId.isNotBlank())
        require(role in setOf("CAREGIVER", "OLDER_ADULT", "LINK_SETUP"))
    }

    /** Stable across renewed credentials and distinct for different subject roles. */
    val storageKey: String
        get() = "$role:$subjectId"
}
