package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryOutcome
import java.time.Instant

/** Converts recorded outcomes to typed history evidence without date or language formatting. */
internal fun HistoryResponse.toHistoryEntry(): IntakeHistoryEntry {
    require(!medicationName.isNullOrBlank())
    return IntakeHistoryEntry(
        medicationName,
        Instant.parse(scheduledAt),
        IntakeHistoryOutcome.valueOf(status),
    )
}
