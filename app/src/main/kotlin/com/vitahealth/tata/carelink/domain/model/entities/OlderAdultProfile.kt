package com.vitahealth.tata.carelink.domain.model.entities

import java.time.LocalDate

/** Personal and emergency-contact details shown during the linking flow. */
data class OlderAdultProfile(
    val id: String,
    val fullName: String,
    val birthDate: LocalDate,
    val emergencyContactName: String?,
    val emergencyContactRelationship: String?,
    val emergencyContactPhone: String?,
) {
    init {
        require(id.isNotBlank()) { "A OlderAdultProfile requires id" }
        require(fullName.isNotBlank()) { "A OlderAdultProfile requires fullName" }
    }
}
