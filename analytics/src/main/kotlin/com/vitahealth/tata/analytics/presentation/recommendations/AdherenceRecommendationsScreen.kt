package com.vitahealth.tata.analytics.presentation.recommendations

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecommendationReadModel
import com.vitahealth.tata.analytics.presentation.components.AnalyticsEmptyStateCard
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataTheme

private val PatternCardStart = Color(0xFFF1ECFF)
private val PatternCardEnd = Color(0xFFECF4FB)
private val HeatmapLabel = Color(0xFF637087)
private val HeatCell = Color(0xFF0E2952)
private val BadgeColors = listOf(Color(0xFFE8F5EB), Color(0xFFFFF3E2), Color(0xFFECF4FB))
private val OutlinedActionColor = Color(0xFF21457A)

@Composable
fun AdherenceRecommendationsRoute(
    factory: AdherenceRecommendationsViewModel.Factory,
    onApplyAdjustments: () -> Unit = {},
    onBackToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: AdherenceRecommendationsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    AdherenceRecommendationsScreen(
        state = state,
        onApplyAdjustments = onApplyAdjustments,
        onBackToHistory = onBackToHistory,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun AdherenceRecommendationsScreen(
    state: AdherenceRecommendationsUiState,
    modifier: Modifier = Modifier,
    onApplyAdjustments: () -> Unit = {},
    onBackToHistory: () -> Unit = {},
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
        Text(
            text = "Recomendaciones",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TataText,
        )
        Text(
            text = "Basadas en patrones de los últimos $RECOMMENDATIONS_PERIOD_DAYS días.",
            fontSize = 12.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(20.dp))

        when (state) {
            AdherenceRecommendationsUiState.Loading -> LoadingCard()
            is AdherenceRecommendationsUiState.Content -> RecommendationsContent(
                data = state.data,
                onApplyAdjustments = onApplyAdjustments,
            )
            AdherenceRecommendationsUiState.InsufficientEvidence -> {
                AnalyticsEmptyStateCard(
                    title = "Evidencia insuficiente",
                    message = "Todavía no hay datos suficientes para presentar una recomendación concluyente.",
                )
                Spacer(Modifier.height(20.dp))
                OutlinedActionButton(text = "Volver a historial", onClick = onBackToHistory)
            }
            is AdherenceRecommendationsUiState.Error -> ErrorCard(message = state.message, onRetry = onRetry)
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun RecommendationsContent(
    data: AdherenceRecommendationsReadModel,
    onApplyAdjustments: () -> Unit,
) {
    PatternCard(data = data)
    Spacer(Modifier.height(20.dp))
    data.recommendations.forEachIndexed { index, recommendation ->
        RecommendationCard(
            number = index + 1,
            recommendation = recommendation,
            badgeColor = BadgeColors[index % BadgeColors.size],
        )
        Spacer(Modifier.height(14.dp))
    }
    Spacer(Modifier.height(6.dp))
    TataButton(text = "Aplicar ajustes", onClick = onApplyAdjustments)
}

@Composable
private fun PatternCard(data: AdherenceRecommendationsReadModel) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape)
            .background(Brush.horizontalGradient(listOf(PatternCardStart, PatternCardEnd)), shape)
            .padding(16.dp),
    ) {
        Text(
            text = data.patternTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = data.patternSummary,
            fontSize = 12.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Concentración por horario",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = HeatmapLabel,
            modifier = Modifier.padding(top = 24.dp),
        )
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
            data.concentration.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { intensity ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .background(HeatCell.copy(alpha = intensity), RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    number: Int,
    recommendation: RecommendationReadModel,
    badgeColor: Color,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape)
            .background(Color.White, shape)
            .border(BorderStroke(1.dp, TataBorder), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$number. ${recommendation.title}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
            )
            Text(
                text = recommendation.description,
                fontSize = 12.sp,
                color = TataMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .size(34.dp)
                .background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TataDeepNavy,
            )
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(25.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.White, shape)
            .border(BorderStroke(1.dp, OutlinedActionColor), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OutlinedActionColor,
        )
    }
}

@Composable
private fun LoadingCard() {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Recomendaciones", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Analizando tus patrones...",
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
            Text("No pudimos cargar las recomendaciones", color = TataText, fontWeight = FontWeight.SemiBold)
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

private val previewData = AdherenceRecommendationsReadModel(
    periodDays = 30,
    patternTitle = "Patrón de omisiones por la tarde",
    patternSummary = "4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.",
    concentration = listOf(
        listOf(0.27f, 0.495f, 0.72f, 0.42f, 0.345f, 0.795f, 0.645f),
        listOf(0.27f, 0.42f, 0.645f, 0.57f, 0.345f, 0.72f, 0.795f),
        listOf(0.195f, 0.345f, 0.57f, 0.495f, 0.27f, 0.645f, 0.72f),
    ),
    recommendations = listOf(
        RecommendationReadModel("Ajusta el recordatorio", "Prueba avisar 15 minutos antes de la toma."),
        RecommendationReadModel("Revisa el horario", "Elige una hora asociada a una rutina estable."),
        RecommendationReadModel("Acompaña durante una semana", "Comprueba si el nuevo horario reduce retrasos."),
    ),
)

@Preview(name = "Con recomendaciones", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceRecommendationsContentPreview() {
    TataTheme {
        AdherenceRecommendationsScreen(state = AdherenceRecommendationsUiState.Content(previewData))
    }
}

@Preview(name = "Evidencia insuficiente", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceRecommendationsInsufficientPreview() {
    TataTheme {
        AdherenceRecommendationsScreen(state = AdherenceRecommendationsUiState.InsufficientEvidence)
    }
}

@Preview(name = "Cargando", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceRecommendationsLoadingPreview() {
    TataTheme {
        AdherenceRecommendationsScreen(state = AdherenceRecommendationsUiState.Loading)
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceRecommendationsErrorPreview() {
    TataTheme {
        AdherenceRecommendationsScreen(
            state = AdherenceRecommendationsUiState.Error("No hay conexión. Inténtalo nuevamente."),
        )
    }
}
