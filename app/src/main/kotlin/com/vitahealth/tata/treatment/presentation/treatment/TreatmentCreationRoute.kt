package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentCreationScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentCreationViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.treatment.domain.model.entities.Treatment

/** Connects draft treatment creation with the next regimen configuration step. */
@Composable
fun TreatmentCreationRoute(
    factory: TreatmentCreationViewModel.Factory,
    modifier: Modifier = Modifier,
    onCreated: (Treatment) -> Unit = {},
) {
    val viewModel: TreatmentCreationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createdTreatment?.id) { state.createdTreatment?.let(onCreated) }

    TreatmentCreationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onCreate = viewModel::createTreatment,
        modifier = modifier,
    )
}
