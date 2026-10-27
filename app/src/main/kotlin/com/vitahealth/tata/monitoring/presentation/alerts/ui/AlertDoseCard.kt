package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

@Composable
internal fun AlertDoseCard(
    dose: MonitoredDose,
    scheduled: String,
    name: String,
    onOpen: () -> Unit,
    remainingMinutes: Long? = null,
) {
    val upcoming = dose.status == "PENDING"
    val colors =
        when {
            LocalTataAccessibility.current.darkTheme ->
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                )
            upcoming -> listOf(Color(0xFFF1ECFF), Color(0xFFE7DFFC))
            else -> listOf(Color(0xFFEAF5FB), Color(0xFFDDECF7))
        }
    val action = stringResource(R.string.alerts_view_dose)
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
            .clickable(role = Role.Button, onClickLabel = action, onClick = onOpen)
            .heightIn(min = if (upcoming) 106.dp else 93.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            TataSvgIcon(
                if (upcoming) R.raw.alerts_reminder else R.raw.alerts_check,
                Modifier.size(24.dp),
            )
        }
        Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                stringResource(
                    when {
                        upcoming -> R.string.alerts_dose_upcoming
                        dose.status == "LATE" -> R.string.alerts_dose_late
                        else -> R.string.alerts_dose_confirmed
                    }
                ),
                color = tataTextColor(),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (name.isBlank()) scheduled
                else stringResource(R.string.alerts_timeline_person, scheduled, name),
                color = AlertsSecondaryText,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    dose.medicationName,
                    modifier = Modifier.weight(1f),
                    color = tataTextColor(),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!upcoming) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.alerts_confirmed_badge),
                        modifier =
                            Modifier.background(Color(0xFFECF7EE), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color(0xFF36764D),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
            if (upcoming && remainingMinutes != null) {
                Spacer(Modifier.height(5.dp))
                Text(
                    if (remainingMinutes < 60)
                        stringResource(R.string.alerts_due_minutes, remainingMinutes)
                    else
                        stringResource(
                            R.string.alerts_due_hours_minutes,
                            remainingMinutes / 60,
                            remainingMinutes % 60,
                        ),
                    color = AlertsSecondaryText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        Text("›", color = tataAccentColor(TataNavy), fontSize = 20.sp)
    }
}
