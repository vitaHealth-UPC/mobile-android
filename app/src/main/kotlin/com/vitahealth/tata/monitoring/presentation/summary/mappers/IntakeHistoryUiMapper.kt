package com.vitahealth.tata.monitoring.presentation.summary.mappers

import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryOutcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats the history dialog while keeping device time and copy out of the remote adapter. */
internal fun IntakeHistoryEntry.toHistoryRow(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.forLanguageTag("es-PE"),
): String {
    val label =
        when (outcome) {
            IntakeHistoryOutcome.CONFIRMED -> "Confirmada"
            IntakeHistoryOutcome.LATE -> "Confirmada con retraso"
            IntakeHistoryOutcome.OMITTED -> "Omitida"
        }
    val time = scheduledAt.atZone(zone).format(DateTimeFormatter.ofPattern("d MMM, h:mm a", locale))
    return "$medicationName · $label\n$time"
}
