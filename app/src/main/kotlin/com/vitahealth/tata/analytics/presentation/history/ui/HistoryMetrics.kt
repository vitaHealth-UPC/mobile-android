package com.vitahealth.tata.analytics.presentation.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceSummaryUi
import com.vitahealth.tata.analytics.presentation.history.viewmodel.label
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText

/** Historical adherence metrics and their visual cards. */

@Composable
internal fun MetricsRow(summary: AdherenceSummaryUi) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MetricCard(
            label = "Adherencia",
            value = "${summary.adherencePercent}%",
            caption = summary.adherenceChangeText,
            startColor = AdherenceCardStart,
            endColor = AdherenceCardEnd,
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Tomas a tiempo",
            value = "${summary.onTimePercent}%",
            caption = summary.onTimeChangeText,
            startColor = OnTimeCardStart,
            endColor = OnTimeCardEnd,
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Tardías, omitidas",
            value = "${summary.lateCount}, ${summary.omittedCount}",
            caption = summary.lateOmittedCaption,
            startColor = LateOmittedCardStart,
            endColor = LateOmittedCardEnd,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    caption: String?,
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .heightIn(min = 92.dp)
                .shadow(elevation = 3.dp, shape = shape)
                .background(Brush.horizontalGradient(listOf(startColor, endColor)), shape)
                .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TataText,
        )
        Text(
            text = value,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TataText,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (caption != null) {
            Text(
                text = caption,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                color = TataDeepNavy,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
