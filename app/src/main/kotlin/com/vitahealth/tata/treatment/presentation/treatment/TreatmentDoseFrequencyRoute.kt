package com.vitahealth.tata.treatment.presentation.treatment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.RegimenBasics
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentDoseFrequencyScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDoseFrequencyViewModel

/** Connects dose and frequency entry to the regimen validation step. */
@Composable
fun TreatmentDoseFrequencyRoute(
    factory: TreatmentDoseFrequencyViewModel.Factory,
    modifier: Modifier = Modifier,
    onCompleted: (RegimenBasics) -> Unit = {},
    initialFrequency: String = "",
) {
    val viewModel: TreatmentDoseFrequencyViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { viewModel.initializeFrequency(initialFrequency) }

    LaunchedEffect(state.validatedBasics) {
        state.validatedBasics?.let { result ->
            onCompleted(result)
            viewModel.consumeCompletion()
        }
    }

    TreatmentDoseFrequencyScreen(
        state = state,
        onDosageChange = viewModel::onDosageChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onContinue = viewModel::continueConfiguration,
        modifier = modifier,
    )
}
