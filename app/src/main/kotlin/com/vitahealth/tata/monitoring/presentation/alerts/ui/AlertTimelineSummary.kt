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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

/** Reports follow-up counts without inferring an unsupported clinical severity. */
@Composable
internal fun AlertTimelineSummary(pendingCount: Int) {
    val clear = pendingCount == 0
    val colors =
        when {
            LocalTataAccessibility.current.darkTheme ->
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                )
            clear -> listOf(Color(0xFFECF8EF), Color(0xFFDDEFE2))
            else -> listOf(Color(0xFFFFF0E8), Color(0xFFFBE0D1))
        }
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(colors))
                .heightIn(min = 86.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlertMark(if (clear) AlertStatus.ATTENDED else AlertStatus.OPEN)
        Column(Modifier.weight(1f).padding(start = 16.dp, end = 4.dp)) {
            Text(
                if (clear) stringResource(R.string.alerts_no_pending)
                else
                    pluralStringResource(
                        R.plurals.alerts_pending_summary,
                        pendingCount,
                        pendingCount,
                    ),
                color = tataTextColor(),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(
                    if (clear) R.string.alerts_empty_message else R.string.alerts_subtitle
                ),
                color = AlertsSecondaryText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        if (clear)
            Image(
                painterResource(R.drawable.alerts_botanical),
                contentDescription = null,
                modifier = Modifier.width(58.dp).height(72.dp),
            )
    }
}
