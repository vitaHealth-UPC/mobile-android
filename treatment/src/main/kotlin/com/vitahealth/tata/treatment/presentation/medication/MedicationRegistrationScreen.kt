package com.vitahealth.tata.treatment.presentation.medication

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.treatment.R

@Composable
fun MedicationRegistrationRoute(
    factory: MedicationRegistrationViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: MedicationRegistrationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    MedicationRegistrationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPresentationChange = viewModel::onPresentationChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onTimingChange = viewModel::onTimingChange,
        onNotesChange = viewModel::onNotesChange,
        onSubmit = viewModel::registerMedication,
        modifier = modifier,
    )
}

@Composable
fun MedicationRegistrationScreen(
    state: MedicationRegistrationUiState,
    onNameChange: (String) -> Unit,
    onPresentationChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onTimingChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
            text = "Agregar medicamento: " + displayName(state.olderAdultName),
            style = MaterialTheme.typography.headlineSmall,
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Cuidador · Paso 1 de 4",
            style = MaterialTheme.typography.bodySmall,
            color = TataMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp),
        )

        Image(
            painter = painterResource(R.drawable.medication_bottle),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp)
                .size(width = 145.dp, height = 160.dp),
        )

        Text(
            text = "Información básica",
            style = MaterialTheme.typography.titleMedium,
            color = TataText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        MedicationField(
            label = "Nombre del medicamento",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Ej. Losartán",
            enabled = state.registeredMedication == null,
        )
        MedicationField(
            label = "Dosis y presentación",
            value = state.presentation,
            onValueChange = onPresentationChange,
            placeholder = "Ej. 50 mg, comprimido",
            enabled = state.registeredMedication == null,
        )
        MedicationField(
            label = "Frecuencia",
            value = state.frequency,
            onValueChange = onFrequencyChange,
            placeholder = "¿Cada cuánto lo tomas?",
            enabled = state.registeredMedication == null,
        )
        MedicationField(
            label = "¿Cuándo lo tomas?",
            value = state.timing,
            onValueChange = onTimingChange,
            placeholder = "Ej. Con el desayuno",
            enabled = state.registeredMedication == null,
        )
        MedicationField(
            label = "Notas opcionales",
            value = state.notes,
            onValueChange = onNotesChange,
            placeholder = "Algo importante que recordar",
            enabled = state.registeredMedication == null,
        )

        state.errorMessage?.let { message ->
            TataCard(
                containerColor = TataErrorSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(
                    text = message,
                    color = TataError,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        state.registeredMedication?.let { medication ->
            TataCard(
                containerColor = TataMint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(
                    text = "Medicamento registrado",
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = medication.name + " · " + medication.presentation,
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        TataButton(
            text = when {
                state.isLoading -> "Guardando..."
                state.registeredMedication != null -> "Medicamento registrado"
                else -> "Siguiente"
            },
            enabled = !state.isLoading && state.registeredMedication == null,
            onClick = onSubmit,
            modifier = Modifier.padding(top = 18.dp),
        )

        StepDots(
            current = 1,
            total = 4,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 22.dp),
        )
    }
}

@Composable
private fun MedicationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(15.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            ),
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = TataText,
            unfocusedTextColor = TataText,
            disabledTextColor = TataMuted,
            focusedLabelColor = TataText,
            unfocusedLabelColor = TataText,
        ),
    )
}

@Composable
private fun StepDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index + 1 == current) 10.dp else 8.dp)
                    .background(
                        color = if (index + 1 == current) TataNavy else Color(0xFFD9DDE7),
                        shape = RoundedCornerShape(99.dp),
                    ),
            )
        }
    }
}

private fun displayName(fullName: String): String =
    fullName.trim().substringBefore(" ").ifBlank { "adulto mayor" }
