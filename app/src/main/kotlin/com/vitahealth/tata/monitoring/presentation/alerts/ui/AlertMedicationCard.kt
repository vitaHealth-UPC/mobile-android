package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

@Composable
internal fun MedicationCard(alert: CaregiverAlert, scheduled: String, dose: String? = null) {
    val colors =
        when {
            LocalTataAccessibility.current.darkTheme ->
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                )
            alert.status == AlertStatus.OPEN ->
                listOf(Color(0xFFFFF0E8), Color(0xFFFADFD0))
            else -> listOf(alert.status.surface(), Color.White)
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
            .heightIn(min = 126.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            TataSvgIcon(
                if (alert.status == AlertStatus.OPEN) R.raw.alerts_warning else R.raw.alerts_check,
                Modifier.size(24.dp),
            )
        }
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(
                alert.medicationName,
                color = tataTextColor(),
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                dose ?: stringResource(R.string.alert_detail_scheduled),
                color = tataTextColor(),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                scheduled,
                color = tataTextColor(),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Image(painterResource(R.drawable.alert_tablet), null, Modifier.size(88.dp))
    }
}
