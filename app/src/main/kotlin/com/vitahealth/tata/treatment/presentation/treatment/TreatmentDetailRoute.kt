package com.vitahealth.tata.treatment.presentation.treatment

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentDetailScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailViewModel

/** Connects treatment detail loading and navigation to the read-only regimen view. */
@Composable
fun TreatmentDetailRoute(
    factory: TreatmentDetailViewModel.Factory,
    onBack: () -> Unit,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
    modifier: Modifier = Modifier,
    onManageTreatment: ((TreatmentDetailReadModel, String) -> Unit)? = null,
    onRestrictedBack: () -> Unit = onBack,
    navigationBar: @Composable () -> Unit = {},
) {
    val viewModel: TreatmentDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    BackHandler(enabled = state is TreatmentDetailUiState.Restricted, onBack = onRestrictedBack)
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.retry()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    TreatmentDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenInventory = onOpenInventory,
        modifier = modifier,
        onManageTreatment = onManageTreatment,
        onRestrictedBack = onRestrictedBack,
        navigationBar = navigationBar,
    )
}
