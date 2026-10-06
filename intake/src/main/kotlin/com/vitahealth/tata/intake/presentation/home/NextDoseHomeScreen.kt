package com.vitahealth.tata.intake.presentation.home

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
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import java.time.Duration
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NextDoseHomeRoute(
    factory: NextDoseHomeViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: NextDoseHomeViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    NextDoseHomeScreen(
        state = state,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun NextDoseHomeScreen(
    state: NextDoseHomeUiState,
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
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Hoy",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TataText,
        )
        Text(
            text = currentDateLabel(),
            style = MaterialTheme.typography.bodyMedium,
            color = TataMuted,
            modifier = Modifier.padding(top = 2.dp),
        )

        when (state) {
            NextDoseHomeUiState.Loading -> LoadingCard()
            is NextDoseHomeUiState.NextDoseAvailable -> NextDoseCard(state.dose)
            is NextDoseHomeUiState.NoNextDose -> NoNextDoseCard()
            is NextDoseHomeUiState.Error -> ErrorCard(state.message, onRetry)
        }

        TipCard()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun LoadingCard() {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text("Próxima toma", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Consultando tu programación...",
            color = TataMuted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun NextDoseCard(dose: NextDoseReadModel) {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Próxima toma",
                    color = TataNavy,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = dose.scheduledAt.atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("h:mm a", Locale("es", "PE")))
                        .lowercase()
                        .replace("am", "a. m.")
                        .replace("pm", "p. m."),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TataText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    text = dose.medicationName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = dose.dose,
                    color = TataMuted,
                    modifier = Modifier.padding(top = 3.dp),
                )
                relativeTimeLabel(dose)?.let { label ->
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .background(Color.White, CircleShape)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = label,
                            color = TataNavy,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, top = 18.dp)
                    .size(86.dp)
                    .background(Color.White.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Rx",
                    color = TataNavy,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (dose.instructions.isNotBlank()) {
            Text(
                text = dose.instructions,
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun NoNextDoseCard() {
    TataCard(
        containerColor = TataLavender,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(
            text = "Sin próxima toma",
            color = TataNavy,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "No hay tomas pendientes programadas.",
            color = TataText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
    }

    TataCard(
        containerColor = TataMint,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
    ) {
        Text("Agenda al día", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "No existen próximas tomas pendientes.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(
            text = "No pudimos cargar tu próxima toma",
            color = TataText,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            color = TataMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        TataButton(
            text = "Reintentar",
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun TipCard() {
    TataCard(
        containerColor = TataCream,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(
            text = "Consejo del día",
            color = TataNavy,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Toma tus medicamentos con agua y a la misma hora cada día.",
            color = TataText,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun currentDateLabel(): String =
    java.time.LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "PE")),
    ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }

private fun relativeTimeLabel(dose: NextDoseReadModel): String? {
    val minutes = Duration.between(java.time.Instant.now(), dose.scheduledAt).toMinutes()
    if (minutes < 0) return null
    return when {
        minutes < 60 -> "En $minutes min"
        minutes < 24 * 60 -> "En " + (minutes / 60) + " h"
        else -> null
    }
}
