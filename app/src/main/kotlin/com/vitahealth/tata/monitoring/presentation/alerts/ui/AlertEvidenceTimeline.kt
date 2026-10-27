package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Presents recorded facts only; scheduling never implies a reminder was sent. */
@Composable
internal fun AlertEvidenceTimeline(
    alert: CaregiverAlert,
    formatter: DateTimeFormatter,
    history: List<RecordedAlertEvent>? = null,
) {
    val shortTime =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(LocalConfiguration.current.locales[0])
            .withZone(ZoneId.systemDefault())
    val scheduledDay = alert.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDate()
    fun eventTime(time: Instant): String =
        if (time.atZone(ZoneId.systemDefault()).toLocalDate() == scheduledDay)
            shortTime.format(time)
        else formatter.format(time)
    Column {
        Text(
            stringResource(R.string.alert_timeline_title),
            color = tataTextColor(),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(14.dp))
        val events =
            history
                ?: listOf(
                    RecordedAlertEvent(RecordedAlertEvent.Type.INTAKE_SCHEDULED, alert.scheduledAt),
                    RecordedAlertEvent(RecordedAlertEvent.Type.ALERT_GENERATED, alert.openedAt),
                )
        val visibleEvents = events.filter { it.type != RecordedAlertEvent.Type.ALERT_CLOSED }
        val hasFinalStatus =
            alert.closedAt != null || alert.status == AlertStatus.ATTENDED || history == null
        visibleEvents.forEachIndexed { index, event ->
            val label =
                when (event.type) {
                    RecordedAlertEvent.Type.INTAKE_SCHEDULED -> R.string.alert_timeline_scheduled
                    RecordedAlertEvent.Type.REINFORCED_REMINDER_SENT ->
                        R.string.alert_timeline_reinforced_sent
                    RecordedAlertEvent.Type.ALERT_GENERATED -> R.string.alert_timeline_opened
                    RecordedAlertEvent.Type.ALERT_CLOSED -> R.string.alert_timeline_closed
                }
            TimelineEvent(
                eventTime(event.occurredAt),
                stringResource(label),
                when (event.type) {
                    RecordedAlertEvent.Type.ALERT_GENERATED -> R.raw.alerts_timeline_pending
                    RecordedAlertEvent.Type.REINFORCED_REMINDER_SENT -> R.raw.alerts_sent_reminder
                    else -> R.raw.alerts_timeline_scheduled
                },
                index == visibleEvents.lastIndex && !hasFinalStatus,
            )
        }
        val closedAt = alert.closedAt
        if (closedAt != null) {
            TimelineEvent(
                eventTime(closedAt),
                stringResource(R.string.alert_timeline_closed),
                R.raw.alerts_check,
                true,
            )
        } else if (alert.status == AlertStatus.ATTENDED || history == null) {
            TimelineEvent(
                null,
                stringResource(
                    if (alert.status == AlertStatus.ATTENDED) R.string.alert_status_attended
                    else R.string.alert_status_open
                ),
                if (alert.status == AlertStatus.ATTENDED) R.raw.alerts_check
                else R.raw.alerts_timeline_pending,
                true,
            )
        }
    }
}

@Composable
private fun TimelineEvent(time: String?, label: String, icon: Int, last: Boolean) {
    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.width(44.dp).height(44.dp), contentAlignment = Alignment.TopCenter) {
            if (!last) Box(Modifier.width(2.dp).height(44.dp).background(Color(0xFFDDDDE7)))
            Box(
                Modifier.size(26.dp).background(Color(0xFFFCFBFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                TataSvgIcon(icon, Modifier.size(22.dp))
            }
        }
        Row(
            Modifier.weight(1f).padding(start = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (time != null)
                Text(
                    time,
                    modifier = Modifier.weight(0.38f),
                    color = tataTextColor(),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            Text(
                label,
                modifier = Modifier.weight(if (time == null) 1f else 0.62f),
                color = AlertsSecondaryText,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}
