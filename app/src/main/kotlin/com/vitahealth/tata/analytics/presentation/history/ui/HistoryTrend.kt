package com.vitahealth.tata.analytics.presentation.history.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceTrendPoint
import com.vitahealth.tata.analytics.presentation.history.viewmodel.label
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import kotlin.math.ceil

/** Trend visualization and its Canvas drawing implementation. */

@Composable
internal fun TrendCard(points: List<AdherenceTrendPoint>) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(elevation = 3.dp, shape = shape)
                .background(Color.White, shape)
                .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "Evolución de adherencia  ⓘ",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
            )
            points.lastOrNull()?.let { last ->
                Text(
                    text = "${last.adherencePercent}%\n${last.label}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TataDeepNavy,
                    textAlign = TextAlign.End,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        AdherenceTrendChart(points = points)
    }
}

@Composable
private fun AdherenceTrendChart(points: List<AdherenceTrendPoint>) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = ChartLabel)

    Canvas(modifier = Modifier.fillMaxWidth().height(176.dp)) {
        val left = 40.dp.toPx()
        val right = 6.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = 22.dp.toPx()
        val plotWidth = size.width - left - right
        val plotHeight = size.height - top - bottom

        fun yAt(percent: Int): Float = top + plotHeight * (1f - percent.coerceIn(0, 100) / 100f)

        listOf(100, 75, 50, 25, 0).forEach { value ->
            val y = yAt(value)
            drawLine(
                color = ChartGrid,
                start = Offset(left, y),
                end = Offset(left + plotWidth, y),
                strokeWidth = 1.dp.toPx(),
            )
            val layout = measurer.measure("$value%", labelStyle)
            drawText(layout, topLeft = Offset(0f, y - layout.size.height / 2f))
        }

        if (points.isEmpty()) return@Canvas

        val stepX = if (points.size > 1) plotWidth / (points.size - 1) else 0f
        fun xAt(index: Int): Float = left + stepX * index

        val linePath =
            Path().apply {
                points.forEachIndexed { index, point ->
                    val x = xAt(index)
                    val y = yAt(point.adherencePercent)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
        val areaPath =
            Path().apply {
                moveTo(xAt(0), top + plotHeight)
                points.forEachIndexed { index, point ->
                    lineTo(xAt(index), yAt(point.adherencePercent))
                }
                lineTo(xAt(points.lastIndex), top + plotHeight)
                close()
            }

        drawPath(
            path = areaPath,
            brush =
                Brush.verticalGradient(
                    colors = listOf(ChartAreaTop, ChartAreaBottom),
                    startY = top,
                    endY = top + plotHeight,
                ),
        )
        drawPath(
            path = linePath,
            color = ChartLine,
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        points.forEachIndexed { index, point ->
            drawCircle(
                color = ChartDot,
                radius = 3.dp.toPx(),
                center = Offset(xAt(index), yAt(point.adherencePercent)),
            )
        }

        val labelStep = ceil(points.size / 5f).toInt().coerceAtLeast(1)
        points.forEachIndexed { index, point ->
            if (index % labelStep == 0) {
                val layout = measurer.measure(point.label, labelStyle)
                val x =
                    (xAt(index) - layout.size.width / 2f).coerceIn(
                        0f,
                        size.width - layout.size.width,
                    )
                drawText(layout, topLeft = Offset(x, top + plotHeight + 6.dp.toPx()))
            }
        }
    }
}
