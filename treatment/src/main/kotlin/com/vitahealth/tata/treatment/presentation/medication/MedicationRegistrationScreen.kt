package com.vitahealth.tata.treatment.presentation.medication

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.vitahealth.tata.shared.design.components.TataSvgIcon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.vitahealth.tata.treatment.domain.model.Medication

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
    onBack: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = TataNavy, fontSize = 24.sp,
            modifier = Modifier.clickable(onClick = onBack).padding(end = 8.dp)
                .semantics { contentDescription = "Volver" })
        Text(
            text = "Agregar medicamento: " + displayName(state.olderAdultName),
            fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
            fontSize = 22.sp,
            lineHeight = 29.sp,
            color = TataText,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        }
        Text(
            text = "Cuidador · Paso 1 de 4",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp),
            color = TataMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp),
        )

        TataSvgIcon(
            resource = R.raw.medication_bottle,
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
        FrequencyField(state.frequency, onFrequencyChange, !state.isLoading && state.registeredMedication == null)
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
                Text(if (state.name.isBlank() || state.presentation.isBlank()) "Faltan datos obligatorios" else "No pudimos guardar el medicamento", color = TataError,
                    fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
                    fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = message,
                    color = TataError,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp),
                    fontWeight = FontWeight.Normal,
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
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp),
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
private fun FrequencyField(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)
            .shadow(6.dp, RoundedCornerShape(15.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
            .background(Color.White, RoundedCornerShape(15.dp)).clickable(enabled = enabled) { expanded = true }
            .padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("Frecuencia", color = TataText, fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
                fontSize = 10.sp, lineHeight = 13.sp)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value.ifBlank { "¿Cada cuánto lo tomas?" }, color = if (value.isBlank()) TataMuted else TataText,
                    fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 12.sp,
                    lineHeight = 16.sp, modifier = Modifier.weight(1f))
                Text("⌄", color = TataNavy)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Una vez al día", "Cada 8 horas", "Cada 12 horas", "Cada 24 horas", "Según indicación médica").forEach { frequency ->
                DropdownMenuItem(text = { Text(frequency) }, onClick = { onValueChange(frequency); expanded = false })
            }
        }
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
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)
        .shadow(6.dp, RoundedCornerShape(15.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
        .background(Color.White, RoundedCornerShape(15.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(label, color = TataText, fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
            fontSize = 10.sp, lineHeight = 13.sp)
        Spacer(Modifier.height(5.dp))
        BasicTextField(value = value, onValueChange = onValueChange, enabled = enabled, singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
                fontSize = 12.sp, lineHeight = 16.sp, color = TataText),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            decorationBox = { input -> Box {
                if (value.isEmpty()) Text(placeholder, color = TataMuted,
                    fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
                    fontSize = 12.sp, lineHeight = 16.sp)
                input()
            } })
    }
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
