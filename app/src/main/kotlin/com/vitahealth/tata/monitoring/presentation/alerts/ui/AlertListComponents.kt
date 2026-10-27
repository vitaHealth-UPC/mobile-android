package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor

@Composable
internal fun NoAlertsCard() {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = tataTonalColor(TataMint)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlertMark(AlertStatus.ATTENDED)
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text = stringResource(R.string.alerts_empty_title),
                    color = tataTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.alerts_empty_message),
                    color = AlertsSecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Image(
                painterResource(R.drawable.alerts_botanical),
                contentDescription = null,
                modifier = Modifier.width(58.dp).height(72.dp),
            )
        }
    }
}

@Composable
internal fun AlertFilters(selected: AlertFilter, onSelected: (AlertFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlertFilter.entries.forEach { filter ->
            val label =
                when (filter) {
                    AlertFilter.ALL -> R.string.alerts_filter_all
                    AlertFilter.PENDING -> R.string.alerts_filter_pending
                    AlertFilter.UPCOMING -> R.string.alerts_filter_upcoming
                    AlertFilter.COMPLETED -> R.string.alerts_filter_completed
                }
            val active = filter == selected
            Box(
                modifier =
                    Modifier.weight(1f)
                        .heightIn(min = 48.dp)
                        .selectable(
                            selected = active,
                            role = Role.RadioButton,
                            onClick = { onSelected(filter) },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                if (active) tataAccentColor(TataNavy)
                                else if (LocalTataAccessibility.current.darkTheme)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else Color(0xFFF0F0F5)
                            )
                            .heightIn(min = 30.dp)
                            .padding(horizontal = 6.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(label),
                        color =
                            if (active && LocalTataAccessibility.current.darkTheme)
                                MaterialTheme.colorScheme.onPrimary
                            else if (active) Color.White else AlertsSecondaryText,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlertListItem(
    alert: CaregiverAlert,
    scheduled: String,
    onClick: () -> Unit,
    name: String = "",
) {
    val openLabel = stringResource(R.string.alerts_open_detail)
    val colors =
        when {
            LocalTataAccessibility.current.darkTheme ->
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                )
            alert.status == AlertStatus.OPEN ->
                listOf(Color(0xFFFFF0E8), Color(0xFFFBE0D1))
            else -> listOf(Color(0xFFEAF5FB), Color(0xFFDDECF7))
        }
    Row(
        Modifier.fillMaxWidth()
            .shadow(
                6.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(colors))
            .clickable(role = Role.Button, onClickLabel = openLabel, onClick = onClick)
            .heightIn(min = 106.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            TataSvgIcon(
                if (alert.status == AlertStatus.OPEN) R.raw.alerts_warning else R.raw.alerts_check,
                Modifier.size(24.dp),
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                stringResource(
                    if (alert.status == AlertStatus.OPEN) R.string.alerts_dose_pending
                    else alert.status.titleRes()
                ),
                color = tataTextColor(),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (name.isBlank()) stringResource(R.string.alerts_card_scheduled, scheduled)
                else stringResource(R.string.alerts_timeline_person, scheduled, name),
                color = AlertsSecondaryText,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                alert.medicationName,
                color = tataTextColor(),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (alert.reason.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    alert.reason,
                    color = AlertsSecondaryText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (alert.status != AlertStatus.OPEN) {
                Spacer(Modifier.height(6.dp))
                AlertStatusChip(alert.status)
            }
        }
        Text("›", color = tataAccentColor(TataNavy), fontSize = 20.sp, lineHeight = 24.sp)
    }
}
