package com.vitahealth.tata.treatment.presentation.treatment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.ScheduleInstructions
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentScheduleInstructionsScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentScheduleInstructionsViewModel

/** Connects local administration time entry and optional instructions to validation. */
@Composable
fun TreatmentScheduleInstructionsRoute(
    factory: TreatmentScheduleInstructionsViewModel.Factory,
    modifier: Modifier = Modifier,
    onCompleted: (ScheduleInstructions) -> Unit = {},
    initialInstructions: String = "",
) {
    val viewModel: TreatmentScheduleInstructionsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { viewModel.initializeInstructions(initialInstructions) }

    LaunchedEffect(state.validatedSchedule) {
        state.validatedSchedule?.let { result ->
            onCompleted(result)
            viewModel.consumeCompletion()
        }
    }

    TreatmentScheduleInstructionsScreen(
        state = state,
        onScheduleChange = viewModel::onScheduleChange,
        onInstructionsChange = viewModel::onInstructionsChange,
        onContinue = viewModel::continueConfiguration,
        modifier = modifier,
    )
}
