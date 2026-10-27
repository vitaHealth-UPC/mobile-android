package com.vitahealth.tata.preferences.presentation.notifications.mappers

import androidx.compose.runtime.Composable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Formats local time using the user's device locale, leaving the domain clock neutral. */
@Composable
internal fun formatTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
