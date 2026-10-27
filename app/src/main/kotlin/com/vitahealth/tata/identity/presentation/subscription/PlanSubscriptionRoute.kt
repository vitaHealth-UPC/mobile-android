package com.vitahealth.tata.identity.presentation.subscription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.presentation.subscription.screens.PlanSubscriptionScreen
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun PlanSubscriptionRoute(
    factory: PlanSubscriptionViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTabSelected: (CaregiverTab) -> Unit = { onBack() },
    onSignIn: () -> Unit = onBack,
) {
    val viewModel: PlanSubscriptionViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    PlanSubscriptionScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onSelectPlan = viewModel::onSelectPlan,
        onChangePlan = viewModel::onChangeRequest,
        onChangeCancel = viewModel::onChangeCancel,
        onChangeConfirm = viewModel::onChangeConfirm,
        modifier = modifier,
        onTabSelected = onTabSelected,
        onSignIn = onSignIn,
    )
}
