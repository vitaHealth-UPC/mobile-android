package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.mappers.firstNameDetail
import com.vitahealth.tata.treatment.presentation.treatment.mappers.medicationLabel
import com.vitahealth.tata.treatment.presentation.treatment.mappers.statusLabel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailUiState

@Composable
internal fun LoadingState() {
    Text(
        text = "Tratamiento",
        style = MaterialTheme.typography.headlineMedium,
        color = tataTextColor(),
        fontWeight = FontWeight.Bold,
    )
    TataCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Text(text = "Cargando detalle...", color = tataMutedColor())
    }
}

@Composable
internal fun LoadedState(
    state: TreatmentDetailUiState.Loaded,
    onOpenInventory: (medicationId: String, medicationName: String) -> Unit,
    onManageTreatment: ((TreatmentDetailReadModel, String) -> Unit)? = null,
) {
    val detail = state.detail
    Text(
        text = "Tratamiento de " + firstNameDetail(state.olderAdultName),
        style = MaterialTheme.typography.headlineMedium,
        color = tataTextColor(),
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Gestiona la pauta sin perder historial.",
        style = MaterialTheme.typography.bodySmall,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 4.dp),
    )

    TataCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = detail.name, color = tataTextColor(), fontWeight = FontWeight.SemiBold)
                Text(
                    text = medicationLabel(detail, state.medicationLabelHint),
                    color = tataMutedColor(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            TataCard(
                containerColor =
                    when (detail.status) {
                        TreatmentStatus.ACTIVE -> TataMint
                        TreatmentStatus.PAUSED -> TataLavender
                        TreatmentStatus.DRAFT -> TataErrorSurface
                    }
            ) {
                Text(
                    text = statusLabel(detail.status),
                    color = tataTextColor(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    TataCard(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
        DetailRow("Dosis", detail.dose ?: "Sin configurar")
        DetailRow("Frecuencia", detail.frequency ?: "Sin configurar")
        DetailRow(
            "Horarios",
            detail.scheduledTimes.takeIf { it.isNotEmpty() }?.joinToString(" · ")
                ?: "Sin configurar",
        )
        DetailRow("Indicaciones", detail.instructions.ifBlank { "Sin indicaciones adicionales" })
    }

    detail.followUpDelayMinutes?.let { minutes ->
        TataCard(
            containerColor = tataTonalColor(TataLavender),
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        ) {
            Text(
                text = "Recordatorios activos",
                color = tataTextColor(),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Avisar $minutes min después si no confirma.",
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }

    TataCard(
        containerColor = tataTonalColor(TataLavender),
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    ) {
        Text(
            text = "Historial protegido",
            color = tataTextColor(),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Pausar o editar solo afecta las tomas futuras.",
            color = tataMutedColor(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 5.dp),
        )
    }

    if (detail.status == TreatmentStatus.PAUSED) {
        TataCard(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
            Text(
                text = "Tratamiento pausado",
                color = tataTextColor(),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "No se generarán nuevas tomas mientras permanezca pausado.",
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }

    if (
        onManageTreatment != null &&
            detail.status != TreatmentStatus.DRAFT &&
            detail.medicationId != null &&
            detail.dose != null &&
            detail.frequency != null &&
            detail.scheduledTimes.isNotEmpty()
    ) {
        TataButton(
            text = stringResource(R.string.treatment_manage_regimen),
            onClick = {
                onManageTreatment(detail, medicationLabel(detail, state.medicationLabelHint))
            },
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        )
    }

    // Inventory is keyed by medicationId, so only offer it once the medication is configured.
    detail.medicationId?.let { medicationId ->
        TataButton(
            text = stringResource(R.string.treatment_open_inventory),
            onClick = {
                onOpenInventory(medicationId, medicationLabel(detail, state.medicationLabelHint))
            },
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        )
    }
}

@Composable
internal fun RestrictedState(state: TreatmentDetailUiState.Restricted, onBack: () -> Unit) {
    TreatmentRestrictedContent(state.olderAdultName, onBack)
}

@Composable
internal fun NotFoundState(state: TreatmentDetailUiState.NotFound, onBack: () -> Unit) {
    StateHeader(state.olderAdultName)
    CenterStateCard(
        title = "Tratamiento no encontrado",
        copy = "El tratamiento solicitado ya no está disponible.",
    )
    TataButton(text = "Volver", onClick = onBack, modifier = Modifier.padding(top = 18.dp))
}

@Composable
internal fun ErrorState(
    state: TreatmentDetailUiState.Error,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    StateHeader(state.olderAdultName)
    CenterStateCard(title = "No pudimos cargar el tratamiento", copy = state.message)
    TataButton(text = "Reintentar", onClick = onRetry, modifier = Modifier.padding(top = 18.dp))
    TataButton(text = "Volver", onClick = onBack, modifier = Modifier.padding(top = 10.dp))
}

@Composable
internal fun StateHeader(olderAdultName: String) {
    Text(
        text = "Tratamiento de " + firstNameDetail(olderAdultName),
        style = MaterialTheme.typography.headlineMedium,
        color = tataTextColor(),
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Gestiona la pauta sin perder historial.",
        style = MaterialTheme.typography.bodySmall,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
internal fun CenterStateCard(title: String, copy: String) {
    TataCard(modifier = Modifier.fillMaxWidth().padding(top = 120.dp)) {
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .size(72.dp)
                    .background(tataTonalColor(TataLavender), CircleShape)
        )
        Text(
            text = title,
            color = tataTextColor(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 18.dp),
        )
        Text(
            text = copy,
            color = tataMutedColor(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
        )
    }
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Text(text = label, color = tataMutedColor(), style = MaterialTheme.typography.labelSmall)
    Text(
        text = value,
        color = tataTextColor(),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
    )
}
