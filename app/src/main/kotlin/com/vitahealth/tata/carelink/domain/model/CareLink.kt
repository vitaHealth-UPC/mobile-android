package com.vitahealth.tata.carelink.domain.model

import java.time.Instant

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
)
