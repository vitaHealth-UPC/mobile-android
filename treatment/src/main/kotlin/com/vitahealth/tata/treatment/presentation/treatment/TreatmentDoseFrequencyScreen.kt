package com.vitahealth.tata.treatment.presentation.treatment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.treatment.domain.model.RegimenBasics

@Composable
fun TreatmentDoseFrequencyRoute(
    factory: TreatmentDoseFrequencyViewModel.Factory,
    modifier: Modifier = Modifier,
    onCompleted: (RegimenBasics) -> Unit = {},
    initialFrequency: String = "",
) {
    val viewModel: TreatmentDoseFrequencyViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) {
        if (state.frequency.isBlank() && initialFrequency.isNotBlank()) viewModel.onFrequencyChange(initialFrequency)
    }

    LaunchedEffect(state.validatedBasics) {
        state.validatedBasics?.let(onCompleted)
    }

    TreatmentDoseFrequencyScreen(
        state = state,
        onDosageChange = viewModel::onDosageChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onContinue = viewModel::continueConfiguration,
        modifier = modifier,
    )
}

@Composable
fun TreatmentDoseFrequencyScreen(
    state: TreatmentDoseFrequencyUiState,
    onDosageChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Crear tratamiento",
            style = MaterialTheme.typography.headlineMedium,
            color = TataText,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Cuidador · " + state.olderAdultName,
            style = MaterialTheme.typography.bodySmall,
            color = TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )

        DoseFrequencyProgress(
            modifier = Modifier.padding(top = 20.dp),
        )

        TataCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Text(
                text = state.treatmentName,
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = state.medicationLabel,
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        TataFormField(
            label = "Dosis",
            value = state.dosage,
            onValueChange = onDosageChange,
            placeholder = "Ej. 1 comprimido",
            modifier = Modifier.padding(top = 18.dp),
        )
        TataFormField(
            label = "Frecuencia",
            value = state.frequency,
            onValueChange = onFrequencyChange,
            placeholder = "Ej. Cada día",
            modifier = Modifier.padding(top = 14.dp),
        )

        state.errorMessage?.let { message ->
            TataCard(
                containerColor = TataErrorSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Text(
                    text = message,
                    color = TataError,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        TataButton(
            text = "Siguiente",
            onClick = onContinue,
            modifier = Modifier.padding(top = 20.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DoseFrequencyProgress(
    modifier: Modifier = Modifier,
) {
    val steps = listOf("Datos", "Pauta", "Recordatorios", "Revisar")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        steps.forEachIndexed { index, label ->
            val reached = index <= 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (reached) TataNavy else TataBorder,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = if (reached) Color.White else TataMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = label,
                    color = if (index == 1) TataText else TataMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
