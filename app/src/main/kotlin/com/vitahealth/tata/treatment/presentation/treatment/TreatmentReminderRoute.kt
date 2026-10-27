package com.vitahealth.tata.treatment.presentation.treatment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.ReminderPolicy
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentReminderScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentReminderViewModel

/** Connects follow-up delay entry and validation to the reminder wizard step. */
@Composable
fun TreatmentReminderRoute(
    factory: TreatmentReminderViewModel.Factory,
    modifier: Modifier = Modifier,
    onCompleted: (ReminderPolicy) -> Unit = {},
) {
    val viewModel: TreatmentReminderViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.validatedPolicy) {
        state.validatedPolicy?.let { result ->
            onCompleted(result)
            viewModel.consumeCompletion()
        }
    }

    TreatmentReminderScreen(
        state = state,
        onDelayChange = viewModel::onDelayChange,
        onContinue = viewModel::continueConfiguration,
        modifier = modifier,
    )
}
