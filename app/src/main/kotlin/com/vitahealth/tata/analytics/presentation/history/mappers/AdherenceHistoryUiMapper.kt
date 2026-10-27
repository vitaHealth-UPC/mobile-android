package com.vitahealth.tata.analytics.presentation.history.mappers

import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecentIntakeReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceSummaryUi
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceTrendPoint
import com.vitahealth.tata.analytics.presentation.history.ui.RecentIntakeUi
import com.vitahealth.tata.analytics.presentation.history.viewmodel.label
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private val trendDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-PE"))

private fun periodLabel(days: Int): String = "Últimos $days días"

private fun changeText(change: Int?, suffix: String = ""): String? =
    change?.let {
        val arrow =
            when {
                it > 0 -> "↑"
                it < 0 -> "↓"
                else -> "="
            }
        "$arrow ${abs(it)}%$suffix"
    }

/** Formats all history rows relative to a single reference date. */
internal fun AdherenceSummaryReadModel.toUi(today: LocalDate): AdherenceSummaryUi =
    AdherenceSummaryUi(
        periodLabel = periodLabel(periodDays),
        adherencePercent = adherencePercent,
        adherenceChangeText = changeText(adherenceChangePercent, " vs. $periodDays días previos"),
        onTimePercent = onTimePercent,
        onTimeChangeText = changeText(onTimeChangePercent),
        lateCount = lateCount,
        omittedCount = omittedCount,
        lateOmittedCaption = "últ. $periodDays días",
        trend =
            trend.map {
                AdherenceTrendPoint(
                    label = trendDateFormatter.format(it.date).replace(".", ""),
                    adherencePercent = it.adherencePercent,
                )
            },
        recentIntakes = recentIntakes.map { it.toUi(today) },
        pattern = pattern?.toUi(adherencePercent),
    )

private val spanishLocale: Locale = Locale.forLanguageTag("es-PE")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", spanishLocale)

private fun RecentIntakeReadModel.toUi(today: LocalDate): RecentIntakeUi =
    RecentIntakeUi(
        whenLabel = "${dayLabel(scheduledAt.toLocalDate(), today)}, ${timeLabel(scheduledAt)}",
        medicationName = medicationName,
        outcomeText =
            when (status) {
                IntakeOutcomeStatus.OnTime -> "a tiempo"
                IntakeOutcomeStatus.Late -> minutesLate?.let { "$it min tarde" } ?: "tarde"
                IntakeOutcomeStatus.Omitted -> "omitida"
            },
        status = status,
    )

private fun dayLabel(date: LocalDate, today: LocalDate): String {
    return when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else ->
            date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, spanishLocale)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }
    }
}

private fun timeLabel(dateTime: LocalDateTime): String =
    timeFormatter.format(dateTime).replace(' ', ' ').replace(' ', ' ')
