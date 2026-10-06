package com.vitahealth.tata.treatment.presentation.treatment

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.treatment.R

@Composable
fun TreatmentCreationRoute(
    factory: TreatmentCreationViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: TreatmentCreationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    TreatmentCreationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onCreate = viewModel::createTreatment,
        modifier = modifier,
    )
}

@Composable
fun TreatmentCreationScreen(
    state: TreatmentCreationUiState,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
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

        TreatmentProgress(
            modifier = Modifier.padding(top = 20.dp),
        )

        TataFormField(
            label = "Nombre del tratamiento",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Ej. Control de presión",
            enabled = state.createdTreatment == null && !state.isLoading,
            modifier = Modifier.padding(top = 14.dp),
        )

        TataCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Text(
                text = "Medicamento",
                color = TataMuted,
                style = MaterialTheme.typography.labelSmall,
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(TataLavender, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.treatment_medication_icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column {
                    Text(
                        text = state.medicationLabel,
                        color = TataText,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Registrado en el paso anterior.",
                        color = TataMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        TataCard(
            containerColor = TataLavender,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Text(
                text = "Tratamiento en borrador",
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "La dosis, frecuencia, horarios y recordatorios se configuran en los siguientes pasos. Crear el tratamiento todavía no lo activa.",
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
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

        state.createdTreatment?.let { treatment ->
            TataCard(
                containerColor = TataMint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                Text(
                    text = "Tratamiento creado como borrador",
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = treatment.name + " · Borrador",
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "Aún no genera tomas hasta completar la pauta y activarlo.",
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }

        TataButton(
            text = when {
                state.isLoading -> "Creando..."
                state.createdTreatment != null -> "Borrador creado"
                else -> "Crear tratamiento"
            },
            enabled = !state.isLoading && state.createdTreatment == null,
            onClick = onCreate,
            modifier = Modifier.padding(top = 20.dp),
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TreatmentProgress(
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
                        .background(
                            color = if (index == 0) TataNavy else TataBorder,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = if (index == 0) Color.White else TataMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = label,
                    color = if (index == 0) TataText else TataMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
