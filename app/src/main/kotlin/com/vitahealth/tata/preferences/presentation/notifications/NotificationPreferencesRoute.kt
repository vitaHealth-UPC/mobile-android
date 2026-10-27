package com.vitahealth.tata.preferences.presentation.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.preferences.presentation.notifications.screens.NotificationPreferencesScreen
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun NotificationPreferencesRoute(
    factory: NotificationPreferencesViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTabSelected: ((CaregiverTab) -> Unit)? = null,
) {
    val viewModel: NotificationPreferencesViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    NotificationPreferencesScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onQuietHoursEnabledChange = viewModel::onQuietHoursEnabledChange,
        onQuietHoursStartChange = viewModel::onQuietHoursStartChange,
        onQuietHoursEndChange = viewModel::onQuietHoursEndChange,
        onChannelChange = viewModel::onChannelChange,
        onPolicyChange = viewModel::onPolicyChange,
        modifier = modifier,
        onTabSelected = onTabSelected,
    )
}
