package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState

/** Native editor seeded from the current regimen, retaining typed input after server failure. */
@Composable
internal fun TreatmentRegimenDialog(
    state: TreatmentLifecycleUiState,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
) {
    var dose by rememberSaveable(state.treatmentId) { mutableStateOf(state.dosage) }
    var frequency by rememberSaveable(state.treatmentId) { mutableStateOf(state.frequency) }
    var schedule by rememberSaveable(state.treatmentId) { mutableStateOf(state.scheduleText) }
    var instructions by rememberSaveable(state.treatmentId) { mutableStateOf(state.instructions) }
    var delay by
        rememberSaveable(state.treatmentId) {
            mutableStateOf(
                if (state.usesDefaultFollowUp) "" else state.reminderDelayMinutes.toString()
            )
        }
    var submitted by remember { mutableStateOf(false) }
    LaunchedEffect(submitted, state.isLoading, state.errorMessage) {
        if (submitted && !state.isLoading) {
            if (state.errorMessage == null) onDismiss()
            submitted = false
        }
    }
    AlertDialog(
        onDismissRequest = { if (!state.isLoading) onDismiss() },
        title = { Text("Editar pauta") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    dose,
                    { dose = it },
                    label = { Text("Dosis") },
                    enabled = !state.isLoading,
                )
                OutlinedTextField(
                    frequency,
                    { frequency = it },
                    label = { Text("Frecuencia") },
                    enabled = !state.isLoading,
                )
                OutlinedTextField(
                    schedule,
                    { schedule = it },
                    label = { Text("Horarios HH:mm, separados por coma") },
                    enabled = !state.isLoading,
                )
                OutlinedTextField(
                    instructions,
                    { instructions = it },
                    label = { Text("Indicaciones") },
                    enabled = !state.isLoading,
                )
                OutlinedTextField(
                    delay,
                    { delay = it },
                    label = { Text("Avisar después de (min)") },
                    supportingText = { Text("Vacío: usar la configuración del sistema.") },
                    enabled = !state.isLoading,
                )
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.isLoading,
                onClick = {
                    submitted = true
                    onSave(dose, frequency, schedule, instructions, delay)
                },
            ) {
                Text(if (state.isLoading) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(enabled = !state.isLoading, onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
