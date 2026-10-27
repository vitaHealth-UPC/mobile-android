package com.vitahealth.tata.identity.domain.model.entities

import com.vitahealth.tata.identity.domain.model.valueobjects.AccountStatus
/** Caregiver account identity and verification status returned by the backend. */
data class CaregiverAccount(
    val id: String,
    val name: String,
    val email: String,
    val status: AccountStatus,
) {
    init {
        require(id.isNotBlank()) { "valid account id is required" }
        require(name.isNotBlank()) { "nonblank caregiver name is required" }
        require(email.isNotBlank()) { "nonblank email is required" }
    }
}
