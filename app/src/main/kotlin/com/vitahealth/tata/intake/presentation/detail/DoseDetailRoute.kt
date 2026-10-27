package com.vitahealth.tata.intake.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab

@Composable
fun DoseDetailRoute(
    factory: DoseDetailViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: () -> Unit = onBack,
    onAdultTab: ((AdultTab, String) -> Unit)? = null,
) {
    val viewModel: DoseDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    DoseDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onConfirm = viewModel::confirm,
        modifier = modifier,
        onHome = onHome,
        onAdultTab = onAdultTab,
    )
}
