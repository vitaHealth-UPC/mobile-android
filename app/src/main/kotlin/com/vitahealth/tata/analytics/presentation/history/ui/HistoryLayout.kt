package com.vitahealth.tata.analytics.presentation.history.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import com.vitahealth.tata.analytics.presentation.history.ui.PatternUi
import com.vitahealth.tata.analytics.presentation.history.ui.RecentIntakeUi
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryUiState
import com.vitahealth.tata.analytics.presentation.history.viewmodel.label
import com.vitahealth.tata.analytics.presentation.shared.ui.AnalyticsEmptyStateCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText

/** Historical totals, period selector and pattern narratives. */

@Composable
internal fun AdherenceHistoryBody(
    state: AdherenceHistoryUiState,
    selectedPeriod: AdherencePeriod,
    onPeriodSelected: (AdherencePeriod) -> Unit,
    onOpenRecommendations: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
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
                state.summary.pattern?.let { pattern ->
                    Spacer(Modifier.height(12.dp))
                    PatternCard(pattern = pattern, onOpenRecommendations = onOpenRecommendations)
                }
                if (state.summary.recentIntakes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    RecentIntakesCard(intakes = state.summary.recentIntakes)
                }
                if (state.periodUpdated) {
                    Spacer(Modifier.height(12.dp))
                    PeriodUpdatedNotice()
                }
            }
            is AdherenceHistoryUiState.InsufficientData -> {
                Spacer(Modifier.height(112.dp))
                AnalyticsEmptyStateCard(
                    title = "Sin datos suficientes",
                    message = "No existen tomas en este periodo para calcular adherencia.",
                )
            }
            is AdherenceHistoryUiState.NoResults -> {
                Spacer(Modifier.height(112.dp))
                AnalyticsEmptyStateCard(
                    title = "Sin resultados",
                    message = "No existen tomas registradas dentro del periodo seleccionado.",
                )
            }
            is AdherenceHistoryUiState.Error ->
                ErrorCard(message = state.message, onRetry = onRetry)
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
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Normal,
                color = TataText,
            )
            PeriodSelector(selected = selectedPeriod, onSelected = onPeriodSelected)
        }
        CalendarIcon(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun PeriodSelector(selected: AdherencePeriod, onSelected: (AdherencePeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.heightIn(min = 44.dp).clickable { expanded = true },
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
private fun PatternCard(pattern: PatternUi, onOpenRecommendations: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(elevation = 3.dp, shape = shape)
                .background(
                    Brush.horizontalGradient(listOf(PatternCardStart, PatternCardEnd)),
                    shape,
                )
                .padding(horizontal = 15.dp, vertical = 14.dp)
    ) {
        SunIcon(modifier = Modifier.padding(top = 4.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "Patrón detectado",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
            )
            Text(
                text = pattern.headline,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = pattern.summary,
                fontSize = 11.sp,
                color = TataText.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Ver recomendaciones  ›",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PatternLink,
                modifier =
                    Modifier.heightIn(min = 44.dp)
                        .clickable(onClick = onOpenRecommendations)
                        .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun SunIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val unit = size.width / 22f
        val strokeWidth = 1.7f * unit
        val center = Offset(11f * unit, 11f * unit)
        drawCircle(
            color = PatternIcon,
            radius = 3.2f * unit,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        listOf(
                0f to -9.2f,
                0f to 9.2f,
                -9.2f to 0f,
                9.2f to 0f,
                -6.5f to -6.5f,
                6.5f to -6.5f,
                -6.5f to 6.5f,
                6.5f to 6.5f,
            )
            .forEach { (dx, dy) ->
                val inner = Offset(center.x + dx * 0.55f * unit, center.y + dy * 0.55f * unit)
                val outer = Offset(center.x + dx * unit, center.y + dy * unit)
                drawLine(PatternIcon, inner, outer, strokeWidth, StrokeCap.Round)
            }
    }
}

@Composable
private fun RecentIntakesCard(intakes: List<RecentIntakeUi>) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .background(Color.White, shape)
                .border(BorderStroke(1.dp, TataBorder), shape)
                .padding(14.dp)
    ) {
        Text(
            text = "Tomas recientes",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        intakes.forEach { intake ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = intake.whenLabel,
                    fontSize = 11.sp,
                    color = TataMuted,
                    modifier = Modifier.width(104.dp),
                )
                Text(
                    text = "${intake.medicationName}, ${intake.outcomeText}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (intake.status == IntakeOutcomeStatus.Omitted) OmittedText else TataText,
                )
            }
        }
    }
}

@Composable
private fun PeriodUpdatedNotice() {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .background(TataLavender, RoundedCornerShape(16.dp))
                .padding(14.dp)
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
        drawLine(
            TataNavy,
            Offset(6.42f * unit, 2.75f * unit),
            Offset(6.42f * unit, 6.42f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(15.58f * unit, 2.75f * unit),
            Offset(15.58f * unit, 6.42f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(2.75f * unit, 8.25f * unit),
            Offset(19.25f * unit, 8.25f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(6.42f * unit, 11.92f * unit),
            Offset(9.17f * unit, 11.92f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(12.83f * unit, 11.92f * unit),
            Offset(15.58f * unit, 11.92f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(6.42f * unit, 15.58f * unit),
            Offset(9.17f * unit, 15.58f * unit),
            stroke.width,
            StrokeCap.Round,
        )
        drawLine(
            TataNavy,
            Offset(12.83f * unit, 15.58f * unit),
            Offset(15.58f * unit, 15.58f * unit),
            stroke.width,
            StrokeCap.Round,
        )
    }
}
