package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataHighContrastMuted
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSuccess
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal val alertsSerif = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif))

/**
 * Secondary text of the alert screens. `TataMuted` reaches only 3.6:1 on white, below the 4.5:1 of WCAG AA,
 * so these screens use the darker muted token for every normal mode.
 */
internal val AlertsSecondaryText = TataHighContrastMuted

@StringRes
internal fun AlertStatus.labelRes(): Int = when (this) {
    AlertStatus.OPEN -> R.string.alert_status_open
    AlertStatus.ATTENDED -> R.string.alert_status_attended
    AlertStatus.CLOSED -> R.string.alert_status_closed
}

@StringRes
internal fun AlertStatus.titleRes(): Int = when (this) {
    AlertStatus.OPEN -> R.string.alert_title_open
    AlertStatus.ATTENDED -> R.string.alert_title_attended
    AlertStatus.CLOSED -> R.string.alert_title_closed
}

/** Surface of the medication card and of the list item; every pair keeps at least 4.5:1 with its text. */
internal fun AlertStatus.surface(): Color = when (this) {
    AlertStatus.OPEN -> TataErrorSurface
    AlertStatus.ATTENDED -> TataMint
    AlertStatus.CLOSED -> Color.White
}

private fun AlertStatus.chipColors(): Pair<Color, Color> = when (this) {
    AlertStatus.OPEN -> TataWarningSurface to TataWarning
    AlertStatus.ATTENDED -> TataMint to TataSuccess
    AlertStatus.CLOSED -> TataBorder to TataText
}

@Composable
internal fun rememberAlertDateFormatter(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }
}

@Composable
internal fun AlertStatusChip(status: AlertStatus, modifier: Modifier = Modifier) {
    val (background, content) = status.chipColors()
    Text(
        text = stringResource(status.labelRes()),
        color = content,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Original Figma status artwork, kept separate from the native card. */
@Composable
internal fun AlertMark(status: AlertStatus, modifier: Modifier = Modifier) {
    val color = if (status == AlertStatus.OPEN) TataError else TataSuccess
    Box(
        modifier = modifier.size(36.dp).background(Color.White.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        TataSvgIcon(if (status == AlertStatus.OPEN) R.raw.alerts_warning else R.raw.alerts_check, Modifier.size(24.dp))
    }
}

@Composable
internal fun AlertsTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontFamily = alertsSerif,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = tataTextColor(),
        modifier = modifier.semantics { heading() },
    )
}

@Composable
internal fun AlertsBackButton(onBack: () -> Unit) {
    val description = stringResource(R.string.alerts_back)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(role = Role.Button, onClick = onBack)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "‹", fontSize = 30.sp, color = TataNavy)
    }
}

@Composable
internal fun AlertsLoading(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TataNavy, strokeWidth = 3.dp)
        Text(text = message, color = tataTextColor(), modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
internal fun AlertsProblemCard(
    problem: AlertsProblem,
    @StringRes notFoundMessage: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (problem) {
        AlertsProblem.NETWORK -> R.string.alerts_error_network
        AlertsProblem.ACCESS_DENIED -> R.string.alerts_error_access
        AlertsProblem.NOT_FOUND -> notFoundMessage
        AlertsProblem.CONFLICT, AlertsProblem.UNKNOWN -> R.string.alerts_error_unknown
    }
    TataCard(modifier = modifier.fillMaxWidth(), containerColor = TataWarningSurface) {
        Text(text = stringResource(message), color = tataTextColor(), style = MaterialTheme.typography.bodyLarge)
        // Retrying cannot fix a missing link, consent or alert: only transient failures offer it.
        if (problem == AlertsProblem.NETWORK || problem == AlertsProblem.UNKNOWN || problem == AlertsProblem.CONFLICT) {
            TataButton(
                text = stringResource(R.string.alerts_retry),
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
internal fun AlertInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = label, color = AlertsSecondaryText, fontSize = 11.sp, lineHeight = 14.sp, modifier = Modifier.weight(0.3f))
        Text(text = value, color = tataTextColor(), fontSize = 11.sp, lineHeight = 14.sp, modifier = Modifier.weight(0.7f))
    }
}
