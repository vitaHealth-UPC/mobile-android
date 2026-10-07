package com.vitahealth.tata.analytics.presentation.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.analytics.domain.model.AdherencePeriod
import com.vitahealth.tata.analytics.presentation.components.AnalyticsEmptyStateCard
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataTheme
import kotlin.math.ceil

private val AdherenceCardStart = Color(0xFFF0ECFF)
private val AdherenceCardEnd = Color(0xFFE7E0FC)
private val OnTimeCardStart = Color(0xFFECF5FB)
private val OnTimeCardEnd = Color(0xFFDFECF6)
private val LateOmittedCardStart = Color(0xFFECF7EF)
private val LateOmittedCardEnd = Color(0xFFDFF0E4)
private val ChartLabel = Color(0xFF637087)
private val ChartGrid = Color(0xCCE8E8EF)
private val ChartLine = Color(0x618B83E8)
private val ChartDot = Color(0xFF5C6ED1)
private val ChartAreaTop = Color(0x336576E6)
private val ChartAreaBottom = Color(0x006576E6)

@Composable
fun AdherenceHistoryRoute(
    factory: AdherenceHistoryViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdherenceHistoryViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    AdherenceHistoryScreen(
        state = state,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = viewModel::selectPeriod,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun AdherenceHistoryScreen(
    state: AdherenceHistoryUiState,
    modifier: Modifier = Modifier,
    selectedPeriod: AdherencePeriod = AdherencePeriod.LastMonth,
    onPeriodSelected: (AdherencePeriod) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        HistoryHeader(selectedPeriod = selectedPeriod, onPeriodSelected = onPeriodSelected)
        Spacer(Modifier.height(12.dp))

        when (state) {
            AdherenceHistoryUiState.Loading -> LoadingCard()
            is AdherenceHistoryUiState.Content -> {
                MetricsRow(summary = state.summary)
                Spacer(Modifier.height(12.dp))
                TrendCard(points = state.summary.trend)
                if (state.periodUpdated) {
                    Spacer(Modifier.height(12.dp))
                    PeriodUpdatedNotice()
                }
            }
            is AdherenceHistoryUiState.InsufficientData -> AnalyticsEmptyStateCard(
                title = "Sin datos suficientes",
                message = "No existen tomas en este periodo para calcular adherencia.",
            )
            is AdherenceHistoryUiState.NoResults -> AnalyticsEmptyStateCard(
                title = "Sin resultados",
                message = "No existen tomas registradas dentro del periodo seleccionado.",
            )
            is AdherenceHistoryUiState.Error -> ErrorCard(message = state.message, onRetry = onRetry)
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HistoryHeader(
    selectedPeriod: AdherencePeriod,
    onPeriodSelected: (AdherencePeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = "Historial e insights",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TataText,
            )
            PeriodSelector(selected = selectedPeriod, onSelected = onPeriodSelected)
        }
        CalendarIcon(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun PeriodSelector(
    selected: AdherencePeriod,
    onSelected: (AdherencePeriod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clickable { expanded = true },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "${selected.label()} ⌄",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TataDeepNavy,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AdherencePeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.label()) },
                    onClick = {
                        expanded = false
                        onSelected(period)
                    },
                )
            }
        }
    }
}

@Composable
private fun PeriodUpdatedNotice() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TataLavender, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            text = "Periodo actualizado",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = "Los indicadores se calculan solo con las tomas del periodo seleccionado.",
            fontSize = 11.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val unit = size.width / 22f
        val stroke = Stroke(width = 1.7f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = TataNavy,
            topLeft = Offset(2.75f * unit, 4.58f * unit),
            size = Size(16.5f * unit, 14.67f * unit),
            cornerRadius = CornerRadius(1.8f * unit, 1.8f * unit),
            style = stroke,
        )
        drawLine(TataNavy, Offset(6.42f * unit, 2.75f * unit), Offset(6.42f * unit, 6.42f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(15.58f * unit, 2.75f * unit), Offset(15.58f * unit, 6.42f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(2.75f * unit, 8.25f * unit), Offset(19.25f * unit, 8.25f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(6.42f * unit, 11.92f * unit), Offset(9.17f * unit, 11.92f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(12.83f * unit, 11.92f * unit), Offset(15.58f * unit, 11.92f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(6.42f * unit, 15.58f * unit), Offset(9.17f * unit, 15.58f * unit), stroke.width, StrokeCap.Round)
        drawLine(TataNavy, Offset(12.83f * unit, 15.58f * unit), Offset(15.58f * unit, 15.58f * unit), stroke.width, StrokeCap.Round)
    }
}

@Composable
private fun MetricsRow(summary: AdherenceSummaryUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        modifier = modifier
            .height(92.dp)
            .shadow(elevation = 3.dp, shape = shape)
            .background(Brush.horizontalGradient(listOf(startColor, endColor)), shape)
            .padding(12.dp),
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TataText)
        Text(
            text = value,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = TataText,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (caption != null) {
            Text(
                text = caption,
                fontSize = 9.sp,
                color = TataDeepNavy,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TrendCard(points: List<AdherenceTrendPoint>) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape)
            .background(Color.White, shape)
            .padding(16.dp),
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

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp),
    ) {
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

        val linePath = Path().apply {
            points.forEachIndexed { index, point ->
                val x = xAt(index)
                val y = yAt(point.adherencePercent)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        val areaPath = Path().apply {
            moveTo(xAt(0), top + plotHeight)
            points.forEachIndexed { index, point -> lineTo(xAt(index), yAt(point.adherencePercent)) }
            lineTo(xAt(points.lastIndex), top + plotHeight)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
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
            drawCircle(color = ChartDot, radius = 3.dp.toPx(), center = Offset(xAt(index), yAt(point.adherencePercent)))
        }

        val labelStep = ceil(points.size / 5f).toInt().coerceAtLeast(1)
        points.forEachIndexed { index, point ->
            if (index % labelStep == 0) {
                val layout = measurer.measure(point.label, labelStyle)
                val x = (xAt(index) - layout.size.width / 2f)
                    .coerceIn(0f, size.width - layout.size.width)
                drawText(layout, topLeft = Offset(x, top + plotHeight + 6.dp.toPx()))
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Historial", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Consultando tu historial...",
            color = TataMuted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TataCard(modifier = Modifier.fillMaxWidth()) {
            Text("No pudimos cargar el historial", color = TataText, fontWeight = FontWeight.SemiBold)
            Text(
                text = message,
                color = TataMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        TataButton(
            text = "Reintentar",
            onClick = onRetry,
            style = TataButtonStyle.Secondary,
        )
    }
}

private val previewSummary = AdherenceSummaryUi(
    periodLabel = "Últimos 30 días",
    adherencePercent = 92,
    adherenceChangeText = "↑ 8% vs. 30 días previos",
    onTimePercent = 86,
    onTimeChangeText = "↑ 12%",
    lateCount = 6,
    omittedCount = 4,
    lateOmittedCaption = "últ. 30 días",
    trend = listOf(
        AdherenceTrendPoint("20 jun", 57),
        AdherenceTrendPoint("24 jun", 74),
        AdherenceTrendPoint("27 jun", 51),
        AdherenceTrendPoint("4 jul", 79),
        AdherenceTrendPoint("11 jul", 62),
        AdherenceTrendPoint("18 jul", 73),
        AdherenceTrendPoint("20 jul", 92),
    ),
)

@Preview(name = "Con datos", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryContentPreview() {
    TataTheme {
        AdherenceHistoryScreen(state = AdherenceHistoryUiState.Content(previewSummary))
    }
}

private val previewWeekSummary = AdherenceSummaryUi(
    periodLabel = "Últimos 7 días",
    adherencePercent = 92,
    adherenceChangeText = "↑ 4% vs. 7 días previos",
    onTimePercent = 88,
    onTimeChangeText = "↑ 5%",
    lateCount = 3,
    omittedCount = 1,
    lateOmittedCaption = "últ. 7 días",
    trend = listOf(
        AdherenceTrendPoint("30 sep", 88),
        AdherenceTrendPoint("1 oct", 90),
        AdherenceTrendPoint("2 oct", 85),
        AdherenceTrendPoint("3 oct", 95),
        AdherenceTrendPoint("4 oct", 92),
        AdherenceTrendPoint("5 oct", 94),
        AdherenceTrendPoint("6 oct", 92),
    ),
)

@Preview(name = "Periodo actualizado (7 días)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryPeriodUpdatedPreview() {
    TataTheme {
        AdherenceHistoryScreen(
            state = AdherenceHistoryUiState.Content(previewWeekSummary, periodUpdated = true),
            selectedPeriod = AdherencePeriod.LastWeek,
        )
    }
}

@Preview(name = "Sin resultados", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryNoResultsPreview() {
    TataTheme {
        AdherenceHistoryScreen(
            state = AdherenceHistoryUiState.NoResults("Últimos 7 días"),
            selectedPeriod = AdherencePeriod.LastWeek,
        )
    }
}

@Preview(name = "Sin datos suficientes", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryInsufficientDataPreview() {
    TataTheme {
        AdherenceHistoryScreen(state = AdherenceHistoryUiState.InsufficientData("Últimos 30 días"))
    }
}

@Preview(name = "Cargando", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryLoadingPreview() {
    TataTheme {
        AdherenceHistoryScreen(state = AdherenceHistoryUiState.Loading)
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryErrorPreview() {
    TataTheme {
        AdherenceHistoryScreen(
            state = AdherenceHistoryUiState.Error("No hay conexión. Inténtalo nuevamente."),
        )
    }
}
