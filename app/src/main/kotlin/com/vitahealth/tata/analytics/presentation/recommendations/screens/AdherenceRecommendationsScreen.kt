package com.vitahealth.tata.analytics.presentation.recommendations.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.analytics.presentation.recommendations.ui.AdherenceRecommendationsBody
import com.vitahealth.tata.analytics.presentation.recommendations.ui.AdherenceRecommendationsUi
import com.vitahealth.tata.analytics.presentation.recommendations.ui.RecommendationUi
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme

@Composable
fun AdherenceRecommendationsScreen(
    state: AdherenceRecommendationsUiState,
    modifier: Modifier = Modifier,
    onApplyAdjustments: () -> Unit = {},
    onBackToHistory: () -> Unit = {},
    onRetry: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize().background(TataSurface)) {
        AdherenceRecommendationsBody(
            state = state,
            onBackToHistory = onBackToHistory,
            onRetry = onRetry,
            modifier = Modifier.weight(1f),
        )
        if (state is AdherenceRecommendationsUiState.Content) {
            TataButton(
                text = stringResource(R.string.recommendations_apply),
                onClick = onApplyAdjustments,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
            )
        }
        CaregiverTabBar(selected = CaregiverTab.Home, onSelect = onTabSelected)
    }
}

private val previewData =
    AdherenceRecommendationsUi(
        periodDays = 30,
        patternTitle = "Patrón de omisiones por la tarde",
        patternSummary = "4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.",
        concentration =
            listOf(
                listOf(0.27f, 0.495f, 0.72f, 0.42f, 0.345f, 0.795f, 0.645f),
                listOf(0.27f, 0.42f, 0.645f, 0.57f, 0.345f, 0.72f, 0.795f),
                listOf(0.195f, 0.345f, 0.57f, 0.495f, 0.27f, 0.645f, 0.72f),
            ),
        recommendations =
            listOf(
                RecommendationUi(
                    "Ajusta el recordatorio",
                    "Prueba avisar 15 minutos antes de la toma.",
                ),
                RecommendationUi(
                    "Revisa el horario",
                    "Elige una hora asociada a una rutina estable.",
                ),
                RecommendationUi(
                    "Acompaña durante una semana",
                    "Comprueba si el nuevo horario reduce retrasos.",
                ),
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
    TataTheme { AdherenceRecommendationsScreen(state = AdherenceRecommendationsUiState.Loading) }
}

@Preview(name = "Error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceRecommendationsErrorPreview() {
    TataTheme {
        AdherenceRecommendationsScreen(
            state = AdherenceRecommendationsUiState.Error("No hay conexión. Inténtalo nuevamente.")
        )
    }
}
