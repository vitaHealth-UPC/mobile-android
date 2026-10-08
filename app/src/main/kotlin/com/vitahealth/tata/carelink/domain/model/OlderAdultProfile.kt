package com.vitahealth.tata.carelink.domain.model

import java.time.LocalDate

data class OlderAdultProfile(
    val id: String,
    val fullName: String,
    val birthDate: LocalDate,
    val emergencyContactName: String?,
    val emergencyContactRelationship: String?,
    val emergencyContactPhone: String?,
)
