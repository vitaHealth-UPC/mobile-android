package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentListScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/** Displays the adult treatment catalog and forwards navigation actions to its host. */
@Composable
fun TreatmentListRoute(
    factory: TreatmentListViewModel.Factory,
    olderAdultName: String,
    onOpen: (String) -> Unit,
    onAddMedication: () -> Unit,
    onBack: () -> Unit,
) {
    val vm: TreatmentListViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()

    TreatmentListScreen(
        state = state,
        olderAdultName = olderAdultName,
        onOpen = onOpen,
        onAddMedication = onAddMedication,
        onBack = onBack,
        onRetry = vm::refresh,
    )
}
