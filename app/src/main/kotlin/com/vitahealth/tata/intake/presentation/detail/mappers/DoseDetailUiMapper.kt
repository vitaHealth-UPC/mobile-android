package com.vitahealth.tata.intake.presentation.detail.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarningSurface
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun scheduleDayLabel(dose: DoseDetailReadModel): String {
    val locale = LocalConfiguration.current.locales[0]
    return dose.scheduledAt
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE", locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

@Composable
internal fun scheduleTimeLabel(dose: DoseDetailReadModel): String {
    val locale = LocalConfiguration.current.locales[0]
    return dose.scheduledAt
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
}

@Composable
internal fun statusTitle(status: DoseStatus): String =
    stringResource(
        when (status) {
            DoseStatus.PENDING -> R.string.detail_status_pending
            DoseStatus.CONFIRMED -> R.string.detail_status_confirmed
            DoseStatus.LATE -> R.string.detail_status_late
            DoseStatus.OMITTED -> R.string.detail_status_omitted
        }
    )

@Composable
internal fun statusMessage(status: DoseStatus): String =
    stringResource(
        when (status) {
            DoseStatus.PENDING -> R.string.detail_message_pending
            DoseStatus.CONFIRMED -> R.string.detail_message_confirmed
            DoseStatus.LATE -> R.string.detail_message_late
            DoseStatus.OMITTED -> R.string.detail_message_omitted
        }
    )

internal fun statusColor(status: DoseStatus): Color =
    when (status) {
        DoseStatus.PENDING -> TataCream
        DoseStatus.CONFIRMED -> TataMint
        DoseStatus.LATE -> TataCream
        DoseStatus.OMITTED -> TataWarningSurface
    }
