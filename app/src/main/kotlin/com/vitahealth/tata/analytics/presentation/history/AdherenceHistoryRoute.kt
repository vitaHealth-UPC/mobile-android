package com.vitahealth.tata.analytics.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.analytics.presentation.history.screens.AdherenceHistoryScreen
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun AdherenceHistoryRoute(
    factory: AdherenceHistoryViewModel.Factory,
    onOpenRecommendations: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: AdherenceHistoryViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    AdherenceHistoryScreen(
        state = state,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = viewModel::selectPeriod,
        onOpenRecommendations = onOpenRecommendations,
        onRetry = viewModel::retry,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
