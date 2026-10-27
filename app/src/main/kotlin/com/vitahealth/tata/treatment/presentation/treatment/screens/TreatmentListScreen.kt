package com.vitahealth.tata.treatment.presentation.treatment.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListState

/**
 * Renders an immutable treatment catalog without obtaining or creating a ViewModel.
 */
@Composable
fun TreatmentListScreen(
    state: TreatmentListState,
    olderAdultName: String,
    onOpen: (String) -> Unit,
    onAddMedication: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .background(tataBackgroundColor())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Tratamientos de $olderAdultName",
            fontFamily = FontFamily(Font(R.font.tata_serif)),
            fontSize = 29.sp,
            color = tataTextColor(),
        )
        Text("Gestiona la pauta sin perder historial.", fontSize = 12.sp, color = tataMutedColor())
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let {
            TataCard {
                Text(
                    if (it == "CARE_LINK_NOT_AUTHORIZED") "Necesitas un vínculo de cuidado activo."
                    else "No pudimos cargar los tratamientos.",
                    color = TataError,
                )
                TataButton("Reintentar", onRetry)
            }
        }
        if (!state.loading && state.error == null && state.treatments.isEmpty())
            TataCard { Text("Aún no hay tratamientos registrados.", color = tataMutedColor()) }
        state.treatments.forEach { treatment ->
            TataCard {
                Text(
                    treatment.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tataTextColor(),
                )
                Text(
                    when (treatment.status) {
                        TreatmentStatus.ACTIVE -> "Activo"
                        TreatmentStatus.PAUSED -> "Pausado"
                        TreatmentStatus.DRAFT -> "Borrador"
                    },
                    color = tataAccentColor(),
                )
                treatment.dose?.let { Text(it, color = tataTextColor()) }
                if (treatment.scheduledTimes.isNotEmpty())
                    Text(
                        treatment.scheduledTimes.joinToString(" · "),
                        fontSize = 12.sp,
                        color = tataMutedColor(),
                    )
                TataButton(
                    "Ver detalle",
                    { onOpen(treatment.id) },
                    style = TataButtonStyle.Secondary,
                )
            }
        }
        TataCard(containerColor = tataTonalColor(TataCream)) {
            Text("Historial protegido", fontWeight = FontWeight.SemiBold, color = tataTextColor())
            Text(
                "Pausar o editar solo afecta las tomas futuras.",
                fontSize = 12.sp,
                color = tataMutedColor(),
            )
        }
        TataButton("Agregar medicamento", onAddMedication)
        TextButton(onClick = onBack) { Text("Volver al resumen") }
    }
}
