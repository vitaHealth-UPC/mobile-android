package com.vitahealth.tata.treatment.presentation.treatment

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentLifecycleScreen
import com.vitahealth.tata.treatment.presentation.treatment.ui.TreatmentRegimenDialog
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel

/** Connects reviewed regimen configuration and treatment lifecycle actions to the screen. */
@Composable
fun TreatmentLifecycleRoute(
    factory: TreatmentLifecycleViewModel.Factory,
    modifier: Modifier = Modifier,
    onOpenDetail: () -> Unit = {},
    onManageMedication: (() -> Unit)? = null,
    onCompleteConfiguration: (() -> Unit)? = null,
    navigationBar: @Composable (canNavigate: Boolean) -> Unit = {},
) {
    val viewModel: TreatmentLifecycleViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    var reactivating by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable(state.treatmentId) { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.managementReadFailed, state.medicationInactive) {
        if (state.managementReadFailed) editing = false
        if (state.managementReadFailed || !state.medicationInactive) reactivating = false
    }
    if (editing && !state.managementReadFailed && !state.isRefreshingManagement)
        TreatmentRegimenDialog(state, { editing = false }, viewModel::editRegimen)

    // A submitted mutation must finish before leaving; an activated draft opens its real detail.
    BackHandler(enabled = state.isLoading || state.status != TreatmentStatus.DRAFT) {
        if (!state.isLoading) onOpenDetail()
    }

    if (reactivating && !state.managementReadFailed && state.medicationInactive) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!state.isLoading) reactivating = false },
            title = { androidx.compose.material3.Text("Reactivar medicamento") },
            text = {
                androidx.compose.material3.Text(
                    "El medicamento volverá a estar disponible. El tratamiento permanecerá pausado hasta que decidas reanudarlo."
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    enabled = !state.isLoading,
                    onClick = {
                        reactivating = false
                        viewModel.reactivateMedication()
                    },
                ) {
                    androidx.compose.material3.Text("Reactivar")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { reactivating = false }) {
                    androidx.compose.material3.Text("Cancelar")
                }
            },
        )
    }
    TreatmentLifecycleScreen(
        state = state,
        onActivate = viewModel::activate,
        onReactivateMedication = { reactivating = true },
        onCompleteConfiguration = onCompleteConfiguration,
        onPause = viewModel::pause,
        onResume = viewModel::resume,
        onOpenDetail = onOpenDetail,
        onEditRegimen = { editing = true },
        onRetryManagement = viewModel::refreshManagement,
        onManageMedication =
            onManageMedication?.let { navigate ->
                {
                    if (
                        !state.isLoading &&
                            !state.managementReadFailed &&
                            !state.isRefreshingManagement &&
                            !editing &&
                            state.status != TreatmentStatus.DRAFT
                    ) {
                        navigate()
                    }
                }
            },
        modifier = modifier,
        navigationBar = { navigationBar(!state.isLoading && !editing && !reactivating) },
    )
}
