package com.vitahealth.tata.treatment.presentation.treatment.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentReminderUiState

@Composable
fun TreatmentReminderScreen(
    state: TreatmentReminderUiState,
    onDelayChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(tataBackgroundColor())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Crear tratamiento",
            style = MaterialTheme.typography.headlineMedium,
            color = tataTextColor(),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Cuidador · " + state.olderAdultName,
            style = MaterialTheme.typography.bodySmall,
            color = tataMutedColor(),
            modifier = Modifier.padding(top = 4.dp),
        )

        ReminderProgress(modifier = Modifier.padding(top = 20.dp))

        TataCard(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
            Text(text = state.treatmentName, color = tataTextColor(), fontWeight = FontWeight.SemiBold)
            Text(
                text = state.medicationLabel,
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = state.dosage + " · " + state.frequency,
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "Horarios: " + state.scheduleText.replace(",", " · "),
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        TataCard(
            containerColor = tataTonalColor(TataLavender),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Text(text = "Recordatorios activos", color = tataTextColor(), fontWeight = FontWeight.SemiBold)
            Text(
                text = reminderCopy(state.followUpDelayMinutes),
                color = tataMutedColor(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        TataFormField(
            label = "Avisar después de (min)",
            value = state.followUpDelayMinutes,
            onValueChange = onDelayChange,
            placeholder = "10",
            modifier = Modifier.padding(top = 14.dp),
        )
        Text(
            text =
                "El ajuste se aplicará a recordatorios futuros; no modifica tomas ni resultados históricos.",
            color = tataMutedColor(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 7.dp),
        )

        state.errorMessage?.let { message ->
            TataCard(
                containerColor = tataTonalColor(TataErrorSurface),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            ) {
                Text(text = message, color = TataError, fontWeight = FontWeight.SemiBold)
            }
        }

        TataButton(
            text = if (state.validatedPolicy == null) "Siguiente" else "Recordatorio listo",
            enabled = state.validatedPolicy == null,
            onClick = onContinue,
            modifier = Modifier.padding(top = 20.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReminderProgress(modifier: Modifier = Modifier) {
    val steps = listOf("Datos", "Pauta", "Recordatorios", "Revisar")
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(tataCardColor(), RoundedCornerShape(18.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        steps.forEachIndexed { index, label ->
            val reached = index <= 2
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier.size(20.dp)
                            .background(
                                color = if (reached) tataAccentColor(TataNavy) else tataTonalColor(TataBorder),
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = if (reached) MaterialTheme.colorScheme.onPrimary else tataMutedColor(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = label,
                    color = if (index == 2) tataTextColor() else tataMutedColor(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun reminderCopy(value: String): String {
    val minutes = value.toIntOrNull()
    return if (minutes != null) {
        "Avisar $minutes min después si no confirma."
    } else {
        "Define cuándo reforzar el aviso si no hay confirmación."
    }
}
