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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

@Composable
fun TreatmentDetailRoute(
    factory: TreatmentDetailViewModel.Factory,
    onBack: () -> Unit,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TreatmentDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    TreatmentDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenInventory = onOpenInventory,
        modifier = modifier,
    )
}

@Composable
fun TreatmentDetailScreen(
    state: TreatmentDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
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
        when (state) {
            TreatmentDetailUiState.Loading -> LoadingState()
            is TreatmentDetailUiState.Loaded -> LoadedState(state, onOpenInventory)
            is TreatmentDetailUiState.Restricted -> RestrictedState(state, onBack)
            is TreatmentDetailUiState.NotFound -> NotFoundState(state, onBack)
            is TreatmentDetailUiState.Error -> ErrorState(state, onBack, onRetry)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LoadingState() {
    Text(
        text = "Tratamiento",
        style = MaterialTheme.typography.headlineMedium,
        color = TataText,
        fontWeight = FontWeight.Bold,
    )
    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(
            text = "Cargando detalle...",
            color = TataMuted,
        )
    }
}

@Composable
private fun LoadedState(
    state: TreatmentDetailUiState.Loaded,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
) {
    val detail = state.detail
    Text(
        text = "Tratamiento de " + firstNameDetail(state.olderAdultName),
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
                    text = detail.name,
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = medicationLabel(detail, state.medicationLabelHint),
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            TataCard(
                containerColor = when (detail.status) {
                    TreatmentStatus.ACTIVE -> TataMint
                    TreatmentStatus.PAUSED -> TataLavender
                    TreatmentStatus.DRAFT -> TataErrorSurface
                },
            ) {
                Text(
                    text = statusLabel(detail.status),
                    color = TataText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
    ) {
        DetailRow("Dosis", detail.dose ?: "Sin configurar")
        DetailRow("Frecuencia", detail.frequency ?: "Sin configurar")
        DetailRow(
            "Horarios",
            detail.scheduledTimes.takeIf { it.isNotEmpty() }?.joinToString(" · ") ?: "Sin configurar",
        )
        DetailRow(
            "Indicaciones",
            detail.instructions.ifBlank { "Sin indicaciones adicionales" },
        )
    }

    detail.reminderLeadMinutes?.let { minutes ->
        TataCard(
            containerColor = TataLavender,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Text(
                text = "Recordatorios activos",
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Avisar $minutes min después si no confirma.",
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
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

    if (detail.status == TreatmentStatus.PAUSED) {
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

    // Inventory is keyed by medicationId, so only offer it once the medication is configured.
    detail.medicationId?.let { medicationId ->
        TataButton(
            text = stringResource(R.string.treatment_open_inventory),
            onClick = { onOpenInventory(medicationId, medicationLabel(detail, state.medicationLabelHint)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
        )
    }
}

@Composable
private fun RestrictedState(
    state: TreatmentDetailUiState.Restricted,
    onBack: () -> Unit,
) {
    StateHeader(state.olderAdultName)
    CenterStateCard(
        title = "Acceso restringido",
        copy = "Este tratamiento no pertenece a una persona vinculada a tu cuenta.",
    )
    TataButton(
        text = "Volver a Persona vinculada",
        onClick = onBack,
        modifier = Modifier.padding(top = 18.dp),
    )
}

@Composable
private fun NotFoundState(
    state: TreatmentDetailUiState.NotFound,
    onBack: () -> Unit,
) {
    StateHeader(state.olderAdultName)
    CenterStateCard(
        title = "Tratamiento no encontrado",
        copy = "El tratamiento solicitado ya no está disponible.",
    )
    TataButton(
        text = "Volver",
        onClick = onBack,
        modifier = Modifier.padding(top = 18.dp),
    )
}

@Composable
private fun ErrorState(
    state: TreatmentDetailUiState.Error,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    StateHeader(state.olderAdultName)
    CenterStateCard(
        title = "No pudimos cargar el tratamiento",
        copy = state.message,
    )
    TataButton(
        text = "Reintentar",
        onClick = onRetry,
        modifier = Modifier.padding(top = 18.dp),
    )
    TataButton(
        text = "Volver",
        onClick = onBack,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun StateHeader(olderAdultName: String) {
    Text(
        text = "Tratamiento de " + firstNameDetail(olderAdultName),
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
}

@Composable
private fun CenterStateCard(
    title: String,
    copy: String,
) {
    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(72.dp)
                .background(TataLavender, CircleShape),
        )
        Text(
            text = title,
            color = TataText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 18.dp),
        )
        Text(
            text = copy,
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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

private fun medicationLabel(
    detail: TreatmentDetailReadModel,
    hint: String,
): String =
    hint.ifBlank { detail.medicationId ?: "Medicamento sin configurar" }

private fun statusLabel(status: TreatmentStatus): String =
    when (status) {
        TreatmentStatus.DRAFT -> "Incompleto"
        TreatmentStatus.ACTIVE -> "Activo"
        TreatmentStatus.PAUSED -> "Pausado"
    }

private fun firstNameDetail(fullName: String): String =
    fullName.trim().substringBefore(" ").ifBlank { "la persona vinculada" }
