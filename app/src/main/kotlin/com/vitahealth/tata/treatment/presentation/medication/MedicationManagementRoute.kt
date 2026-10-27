package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.presentation.medication.screens.MedicationManagementScreen
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/** Connects medication maintenance state and navigation callbacks to the caregiver screen. */
@Composable
fun MedicationManagementRoute(
    factory: MedicationManagementViewModel.Factory,
    onBack: () -> Unit,
    onAddMedication: () -> Unit,
    modifier: Modifier = Modifier,
    initialDeactivationMedicationId: String? = null,
) {
    val viewModel: MedicationManagementViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    var initialRequestConsumed by rememberSaveable(initialDeactivationMedicationId) {
        mutableStateOf(false)
    }

    // A route request NEVER posts a mutation. It merely opens the standard
    // confirmation dialog, after a fresh list proves the medication belongs
    // to the authorized adult and remains active.
    LaunchedEffect(
        initialDeactivationMedicationId,
        state.isLoading,
        state.loadFailed,
        state.accessDenied,
        state.isSaving,
        state.medications,
    ) {
        if (
            !initialRequestConsumed &&
                !initialDeactivationMedicationId.isNullOrBlank() &&
                !state.isLoading &&
                !state.loadFailed &&
                !state.accessDenied &&
                !state.isSaving
        ) {
            initialRequestConsumed = true
            state.medications.firstOrNull {
                it.id == initialDeactivationMedicationId && it.active
            }?.let(viewModel::onDeactivateRequest)
        }
    }

    MedicationManagementScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onAddMedication = onAddMedication,
        onEdit = viewModel::onEdit,
        onDraftNameChange = viewModel::onDraftNameChange,
        onDraftPresentationChange = viewModel::onDraftPresentationChange,
        onCancelEdit = viewModel::onCancelEdit,
        onSaveEdit = viewModel::onSaveEdit,
        onDeactivateRequest = viewModel::onDeactivateRequest,
        onDeactivateCancel = viewModel::onDeactivateCancel,
        onDeactivateConfirm = viewModel::onDeactivateConfirm,
        modifier = modifier,
    )
}
