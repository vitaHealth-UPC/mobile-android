package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import java.time.Instant

/**
 * Maps known server states and rejects malformed supplied timestamps before session persistence.
 */
internal fun CareLinkResponse.toDomain(): CareLink {
    require(id.isNotBlank() && caregiverId.isNotBlank() && olderAdultId.isNotBlank())
    return CareLink(
        id = id,
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        status = CareLinkStatus.valueOf(status),
        codeExpiresAt = codeExpiresAt?.let(Instant::parse),
        codeUsedAt = codeUsedAt?.let(Instant::parse),
        consentGranted = requireNotNull(consentGranted),
        consentRecordedAt = consentRecordedAt?.let(Instant::parse),
        confirmedAt = confirmedAt?.let(Instant::parse),
    )
}
