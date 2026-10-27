package com.vitahealth.tata.carelink.domain.model.entities

import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import java.time.Instant

/** Relationship between a caregiver and an adult, including consent and lifecycle timestamps. */
data class CareLink(
    val id: String,
    val caregiverId: String,
    val olderAdultId: String,
    val status: CareLinkStatus,
    val codeExpiresAt: Instant?,
    val codeUsedAt: Instant?,
    val consentGranted: Boolean,
    val consentRecordedAt: Instant?,
    val confirmedAt: Instant?,
) {
    init {
        require(id.isNotBlank()) { "A CareLink requires id" }
        require(caregiverId.isNotBlank()) { "A CareLink requires caregiverId" }
        require(olderAdultId.isNotBlank()) { "A CareLink requires olderAdultId" }
    }
}
