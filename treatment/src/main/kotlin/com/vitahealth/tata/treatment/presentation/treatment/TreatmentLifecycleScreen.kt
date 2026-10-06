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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

@Composable
fun TreatmentLifecycleRoute(
    factory: TreatmentLifecycleViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: TreatmentLifecycleViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    TreatmentLifecycleScreen(
        state = state,
        onActivate = viewModel::activate,
        onPause = viewModel::pause,
        onResume = viewModel::resume,
        modifier = modifier,
    )
}

@Composable
fun TreatmentLifecycleScreen(
    state: TreatmentLifecycleUiState,
    onActivate: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
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
        if (state.status == TreatmentStatus.DRAFT) {
            ReviewTreatment(
                state = state,
                onActivate = onActivate,
            )
        } else {
            ManageTreatment(
                state = state,
                onPause = onPause,
                onResume = onResume,
            )
        }

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

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReviewTreatment(
    state: TreatmentLifecycleUiState,
    onActivate: () -> Unit,
) {
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

    ReviewProgress(modifier = Modifier.padding(top = 20.dp))

    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
    ) {
        Text(state.treatmentName, color = TataText, fontWeight = FontWeight.SemiBold)
        Text(
            text = state.medicationLabel,
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }

    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        SummaryRow("Dosis", state.dosage)
        SummaryRow("Frecuencia", state.frequency)
        SummaryRow("Horarios", state.scheduleText.replace(",", " · "))
        SummaryRow("Indicaciones", state.instructions.ifBlank { "Sin indicaciones adicionales" })
    }

    TataCard(
        containerColor = TataLavender,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Text(
            text = "Recordatorios activos",
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Avisar " + state.reminderDelayMinutes + " min después si no confirma.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
    }

    TataButton(
        text = if (state.isLoading) "Activando..." else "Activar tratamiento",
        enabled = !state.isLoading,
        onClick = onActivate,
        modifier = Modifier.padding(top = 20.dp),
    )
}

@Composable
private fun ManageTreatment(
    state: TreatmentLifecycleUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    Text(
        text = "Tratamiento de " + firstName(state.olderAdultName),
        style = MaterialTheme.typography.headlineMedium,
        color = TataText,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Gestiona la pauta sin perder historial.",
        style = MaterialTheme.typography.bodySmall,
        color = TataMuted,
        modifier = Modifier.padding(top = 4.dp),
    )

    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            TataCard(
                containerColor = if (state.status == TreatmentStatus.ACTIVE) TataMint else TataLavender,
            ) {
                Text(
                    text = if (state.status == TreatmentStatus.ACTIVE) "Activo" else "Pausado",
                    color = TataText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        OutlinedButton(
            onClick = if (state.status == TreatmentStatus.ACTIVE) onPause else onResume,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
        ) {
            Text(
                text = when {
                    state.isLoading -> "Actualizando..."
                    state.status == TreatmentStatus.ACTIVE -> "Pausar"
                    else -> "Reanudar"
                },
            )
        }
    }

    TataCard(
        containerColor = TataLavender,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
    ) {
        Text(
            text = "Historial protegido",
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Pausar o editar solo afecta las tomas futuras.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 5.dp),
        )
    }

    if (state.status == TreatmentStatus.PAUSED) {
        TataCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Text(
                text = "Tratamiento pausado",
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "No se generarán nuevas tomas mientras permanezca pausado.",
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Text(
        text = label,
        color = TataMuted,
        style = MaterialTheme.typography.labelSmall,
    )
    Text(
        text = value,
        color = TataText,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
    )
}

@Composable
private fun ReviewProgress(
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(TataNavy, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = label,
                    color = if (index == 3) TataText else TataMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun firstName(fullName: String): String =
    fullName.trim().substringBefore(" ").ifBlank { "Rosa" }
