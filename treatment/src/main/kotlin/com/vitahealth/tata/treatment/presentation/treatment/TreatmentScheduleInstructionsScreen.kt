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
import com.vitahealth.tata.treatment.domain.model.ScheduleInstructions
import java.time.format.DateTimeFormatter

@Composable
fun TreatmentScheduleInstructionsRoute(
    factory: TreatmentScheduleInstructionsViewModel.Factory,
    modifier: Modifier = Modifier,
    onCompleted: (ScheduleInstructions) -> Unit = {},
) {
    val viewModel: TreatmentScheduleInstructionsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.validatedSchedule) {
        state.validatedSchedule?.let(onCompleted)
    }

    TreatmentScheduleInstructionsScreen(
        state = state,
        onScheduleChange = viewModel::onScheduleChange,
        onInstructionsChange = viewModel::onInstructionsChange,
        onContinue = viewModel::continueConfiguration,
        modifier = modifier,
    )
}

@Composable
fun TreatmentScheduleInstructionsScreen(
    state: TreatmentScheduleInstructionsUiState,
    onScheduleChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
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

        ScheduleProgress(modifier = Modifier.padding(top = 20.dp))

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
            Text(
                text = "Dosis: " + state.dosage + " · Frecuencia: " + state.frequency,
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        TataFormField(
            label = "Horarios (HH:mm)",
            value = state.scheduleText,
            onValueChange = onScheduleChange,
            placeholder = "Ej. 08:00, 13:00, 20:00",
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = "Puedes registrar uno o varios horarios separados por coma.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
        TataFormField(
            label = "Indicaciones",
            value = state.instructions,
            onValueChange = onInstructionsChange,
            placeholder = "Ej. Con agua",
            modifier = Modifier.padding(top = 14.dp),
        )

        state.validatedSchedule?.let { validated ->
            TataCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Text(
                    text = "Vista previa de pauta",
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = validated.schedule.times.joinToString(" · ") {
                        it.format(DateTimeFormatter.ofPattern("HH:mm"))
                    },
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp),
                )
                if (validated.instructions.value.isNotBlank()) {
                    Text(
                        text = validated.instructions.value,
                        color = TataMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
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

        TataButton(
            text = if (state.validatedSchedule == null) "Siguiente" else "Pauta lista",
            enabled = state.validatedSchedule == null,
            onClick = onContinue,
            modifier = Modifier.padding(top = 20.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ScheduleProgress(
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
