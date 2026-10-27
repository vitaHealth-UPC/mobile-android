package com.vitahealth.tata.analytics.presentation.recommendations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.analytics.presentation.recommendations.screens.AdherenceRecommendationsScreen
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun AdherenceRecommendationsRoute(
    factory: AdherenceRecommendationsViewModel.Factory,
    onApplyAdjustments: () -> Unit = {},
    onBackToHistory: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: AdherenceRecommendationsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    AdherenceRecommendationsScreen(
        state = state,
        onApplyAdjustments = onApplyAdjustments,
        onBackToHistory = onBackToHistory,
        onRetry = viewModel::retry,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
