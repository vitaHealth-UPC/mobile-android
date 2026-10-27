package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.presentation.medication.screens.MedicationRegistrationScreen
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationRegistrationUiState
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationRegistrationViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Connects medication registration input, validation and navigation for the selected adult. */
@Composable
fun MedicationRegistrationRoute(
    factory: MedicationRegistrationViewModel.Factory,
    modifier: Modifier = Modifier,
    onRegistered: (Medication, MedicationRegistrationUiState) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val viewModel: MedicationRegistrationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.registeredMedication?.id) {
        state.registeredMedication?.let { onRegistered(it, state) }
    }

    MedicationRegistrationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPresentationChange = viewModel::onPresentationChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onTimingChange = viewModel::onTimingChange,
        onNotesChange = viewModel::onNotesChange,
        onSubmit = viewModel::registerMedication,
        onBack = onBack,
        modifier = modifier,
    )
}
