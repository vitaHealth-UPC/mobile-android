package com.vitahealth.tata.intake.presentation.home.mappers

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun currentDateLabel(locale: Locale): String =
    LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

internal data class RemainingTime(val value: Long, val hours: Boolean = false)

internal fun relativeTimeLabel(dose: NextDoseReadModel): RemainingTime? {
    val minutes = Duration.between(Instant.now(), dose.scheduledAt).toMinutes()
    if (minutes < 0) return null
    return when {
        minutes < 60 -> RemainingTime(minutes)
        minutes < 24 * 60 -> RemainingTime(minutes / 60, hours = true)
        else -> null
    }
}
