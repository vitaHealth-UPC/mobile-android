package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertsScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AlertsRoute(
    factory: AlertsViewModel.Factory,
    onOpenAlert: (Long) -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
    onOpenDoseSummary: (String) -> Unit,
) {
    val model: AlertsViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(owner, model) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                delay(60_000)
                model.updateTime()
            }
        }
    }
    AlertsScreen(
        state = state,
        onRetry = model::refresh,
        onFilterSelected = model::selectFilter,
        onOpenAlert = onOpenAlert,
        onTabSelected = onTabSelected,
        onOpenDoseSummary = {
            (state as? AlertsUiState.Content)
                ?.olderAdultName
                ?.takeIf { it.isNotBlank() }
                ?.let(onOpenDoseSummary)
        },
    )
}
