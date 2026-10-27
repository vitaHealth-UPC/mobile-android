package com.vitahealth.tata.treatment.presentation.treatment.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.presentation.treatment.ui.ErrorState
import com.vitahealth.tata.treatment.presentation.treatment.ui.LoadedState
import com.vitahealth.tata.treatment.presentation.treatment.ui.LoadingState
import com.vitahealth.tata.treatment.presentation.treatment.ui.NotFoundState
import com.vitahealth.tata.treatment.presentation.treatment.ui.RestrictedState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailUiState

@Composable
fun TreatmentDetailScreen(
    state: TreatmentDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
    modifier: Modifier = Modifier,
    onManageTreatment: ((TreatmentDetailReadModel, String) -> Unit)? = null,
    onRestrictedBack: () -> Unit = onBack,
    navigationBar: @Composable () -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(tataBackgroundColor())) {
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            when (state) {
                TreatmentDetailUiState.Loading -> LoadingState()
                is TreatmentDetailUiState.Loaded ->
                    LoadedState(state, onOpenInventory, onManageTreatment)
                is TreatmentDetailUiState.Restricted -> RestrictedState(state, onRestrictedBack)
                is TreatmentDetailUiState.NotFound -> NotFoundState(state, onBack)
                is TreatmentDetailUiState.Error -> ErrorState(state, onBack, onRetry)
            }
            Spacer(Modifier.height(32.dp))
        }
        if (state is TreatmentDetailUiState.Restricted) navigationBar()
    }
}
