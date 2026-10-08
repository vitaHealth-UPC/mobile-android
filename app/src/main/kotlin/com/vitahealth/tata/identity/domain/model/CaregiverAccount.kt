package com.vitahealth.tata.identity.domain.model

data class CaregiverAccount(
    val id: String,
    val name: String,
    val email: String,
    val status: AccountStatus,
)
