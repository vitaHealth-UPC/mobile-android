package com.vitahealth.tata.intake.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.presentation.home.screens.NextDoseHomeScreen
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeViewModel

@Composable
fun NextDoseHomeRoute(
    factory: NextDoseHomeViewModel.Factory,
    onOpenDoseDetail: (String) -> Unit,
    onOpenAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
    onOpenMedications: (() -> Unit)? = null,
    onOpenNotes: (() -> Unit)? = null,
    onOpenVoice: ((String) -> Unit)? = null,
    accountActions: @Composable () -> Unit = {},
) {
    val viewModel: NextDoseHomeViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.retry()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    NextDoseHomeScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpenDoseDetail = onOpenDoseDetail,
        onOpenAgenda = onOpenAgenda,
        modifier = modifier,
        onSignOut = onSignOut,
        onOpenMedications = onOpenMedications,
        onOpenNotes = onOpenNotes,
        onOpenVoice = onOpenVoice,
        accountActions = accountActions,
    )
}
