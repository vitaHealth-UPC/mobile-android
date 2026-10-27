package com.vitahealth.tata.analytics.presentation.history.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceHistoryBody
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceSummaryUi
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceTrendPoint
import com.vitahealth.tata.analytics.presentation.history.ui.PatternUi
import com.vitahealth.tata.analytics.presentation.history.ui.RecentIntakeUi
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme


@Composable
fun AdherenceHistoryScreen(
    state: AdherenceHistoryUiState,
    modifier: Modifier = Modifier,
    selectedPeriod: AdherencePeriod = AdherencePeriod.LastMonth,
    onPeriodSelected: (AdherencePeriod) -> Unit = {},
    onOpenRecommendations: () -> Unit = {},
    onRetry: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
) {
    CompositionLocalProvider(
        LocalTextStyle provides
            TextStyle(fontFamily = FontFamily(Font(R.font.tata_inter)), letterSpacing = 0.sp)
    ) {
        Column(modifier = modifier.fillMaxSize().background(TataSurface)) {
            AdherenceHistoryBody(
                state = state,
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                onOpenRecommendations = onOpenRecommendations,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            CaregiverTabBar(selected = CaregiverTab.Home, onSelect = onTabSelected)
        }
    }
}

private val previewPattern =
    PatternUi(
        headline = "Excelente progreso",
        summary = "4 omisiones y 6 tomas tardías se concentran en la tarde.",
    )

private val previewRecentIntakes =
    listOf(
        RecentIntakeUi("Hoy, 8:00 a. m.", "Losartán", "a tiempo", IntakeOutcomeStatus.OnTime),
        RecentIntakeUi("Ayer, 8:00 p. m.", "Amlodipino", "24 min tarde", IntakeOutcomeStatus.Late),
        RecentIntakeUi("Sáb, 1:00 p. m.", "Vitamina D3", "omitida", IntakeOutcomeStatus.Omitted),
    )

private val previewSummary =
    AdherenceSummaryUi(
        periodLabel = "Últimos 30 días",
        adherencePercent = 92,
        adherenceChangeText = "↑ 8% vs. 30 días previos",
        onTimePercent = 86,
        onTimeChangeText = "↑ 12%",
        lateCount = 6,
        omittedCount = 4,
        lateOmittedCaption = "últ. 30 días",
        pattern = previewPattern,
        trend =
            listOf(
                AdherenceTrendPoint("20 jun", 57),
                AdherenceTrendPoint("24 jun", 74),
                AdherenceTrendPoint("27 jun", 51),
                AdherenceTrendPoint("4 jul", 79),
                AdherenceTrendPoint("11 jul", 62),
                AdherenceTrendPoint("18 jul", 73),
                AdherenceTrendPoint("20 jul", 92),
            ),
        recentIntakes = previewRecentIntakes,
    )

@Preview(name = "Con datos", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryContentPreview() {
    TataTheme { AdherenceHistoryScreen(state = AdherenceHistoryUiState.Content(previewSummary)) }
}

private val previewWeekSummary =
    AdherenceSummaryUi(
        periodLabel = "Últimos 7 días",
        adherencePercent = 92,
        adherenceChangeText = "↑ 4% vs. 7 días previos",
        onTimePercent = 88,
        onTimeChangeText = "↑ 5%",
        lateCount = 3,
        omittedCount = 1,
        lateOmittedCaption = "últ. 7 días",
        pattern = previewPattern,
        trend =
            listOf(
                AdherenceTrendPoint("30 sep", 88),
                AdherenceTrendPoint("1 oct", 90),
                AdherenceTrendPoint("2 oct", 85),
                AdherenceTrendPoint("3 oct", 95),
                AdherenceTrendPoint("4 oct", 92),
                AdherenceTrendPoint("5 oct", 94),
                AdherenceTrendPoint("6 oct", 92),
            ),
        recentIntakes = previewRecentIntakes,
    )

@Preview(
    name = "Periodo actualizado (7 días)",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
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
    TataTheme { AdherenceHistoryScreen(state = AdherenceHistoryUiState.Loading) }
}

@Preview(name = "Error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AdherenceHistoryErrorPreview() {
    TataTheme {
        AdherenceHistoryScreen(
            state = AdherenceHistoryUiState.Error("No hay conexión. Inténtalo nuevamente.")
        )
    }
}
