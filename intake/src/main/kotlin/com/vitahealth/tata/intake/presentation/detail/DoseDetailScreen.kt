package com.vitahealth.tata.intake.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataWarningSurface
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DoseDetailRoute(
    factory: DoseDetailViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DoseDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    DoseDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun DoseDetailScreen(
    state: DoseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Text(
            text = "‹  Detalle del medicamento",
            style = MaterialTheme.typography.headlineSmall,
            color = TataText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onBack),
        )
        Spacer(Modifier.height(20.dp))

        when (state) {
            DoseDetailUiState.Loading -> TataCard(Modifier.fillMaxWidth()) {
                Text("Cargando detalle de la toma...", color = TataMuted)
            }
            is DoseDetailUiState.Error -> TataCard(Modifier.fillMaxWidth()) {
                Text("No pudimos cargar esta toma", fontWeight = FontWeight.Bold, color = TataText)
                Text(state.message, color = TataMuted, modifier = Modifier.padding(top = 8.dp))
                TataButton("Reintentar", onRetry, Modifier.padding(top = 16.dp))
            }
            is DoseDetailUiState.Content -> DoseContent(state.dose)
        }
    }
}

@Composable
private fun DoseContent(dose: DoseDetailReadModel) {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Detalle de toma", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(dose.medicationName, color = TataNavy, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
        Text(dose.dose, color = TataMuted, modifier = Modifier.padding(top = 3.dp))
    }

    Text("Próxima confirmación", color = TataMuted, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
    TataCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(scheduleDayLabel(dose), color = TataMuted)
                Text(
                    scheduleTimeLabel(dose),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TataText,
                )
            }
            Text(
                text = statusTitle(dose.status),
                color = TataNavy,
                modifier = Modifier
                    .background(statusColor(dose.status), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }

    Text("Información", color = TataMuted, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
    TataCard(Modifier.fillMaxWidth()) {
        DetailLine("Dosis", dose.dose)
        DetailLine("Cuándo", scheduleTimeLabel(dose))
        DetailLine("Medicamento", dose.medicationName)
        DetailLine("Indicaciones", dose.instructions.ifBlank { "Sin indicaciones adicionales" })
    }

    TataCard(
        containerColor = TataLavender,
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
    ) {
        Text("Antes de tu toma", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text("✓ Revisa la dosis y el horario", color = TataText, modifier = Modifier.padding(top = 8.dp))
        if (dose.instructions.isNotBlank()) {
            Text("✓ " + dose.instructions, color = TataText, modifier = Modifier.padding(top = 5.dp))
        }
    }

    TataCard(
        containerColor = statusColor(dose.status),
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
    ) {
        Text("Estado: " + statusTitle(dose.status), color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(statusMessage(dose.status), color = TataMuted, modifier = Modifier.padding(top = 5.dp))
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TataMuted)
        Text(value, color = TataText, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
    }
}

private fun scheduleDayLabel(dose: DoseDetailReadModel): String =
    dose.scheduledAt.atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "PE")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }

private fun scheduleTimeLabel(dose: DoseDetailReadModel): String =
    dose.scheduledAt.atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale("es", "PE")))
        .lowercase()
        .replace("am", "a. m.")
        .replace("pm", "p. m.")

private fun statusTitle(status: DoseStatus): String = when (status) {
    DoseStatus.PENDING -> "Pendiente"
    DoseStatus.CONFIRMED -> "Confirmada"
    DoseStatus.LATE -> "Tardía"
    DoseStatus.OMITTED -> "Omitida"
}

private fun statusMessage(status: DoseStatus): String = when (status) {
    DoseStatus.PENDING -> "Aún puedes confirmar esta toma."
    DoseStatus.CONFIRMED -> "La toma fue registrada correctamente."
    DoseStatus.LATE -> "Se confirmó dentro del periodo de tolerancia."
    DoseStatus.OMITTED -> "El periodo permitido terminó sin confirmación."
}

private fun statusColor(status: DoseStatus): Color = when (status) {
    DoseStatus.PENDING -> TataLavender
    DoseStatus.CONFIRMED -> TataMint
    DoseStatus.LATE -> TataCream
    DoseStatus.OMITTED -> TataWarningSurface
}
