package com.vitahealth.tata.carelink.presentation.link

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.carelink.domain.model.OlderAdultProfile
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import java.time.LocalDate
import java.time.Period

@Composable
fun CareLinkRoute(
    factory: CareLinkViewModel.Factory,
    modifier: Modifier = Modifier,
    onConfirmed: (olderAdultId: String, olderAdultName: String) -> Unit = { _, _ -> },
) {
    val viewModel: CareLinkViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.step, state.acceptedLink?.id, state.olderAdult?.id) {
        val link = state.acceptedLink
        val olderAdult = state.olderAdult
        if (state.step == CareLinkStep.Confirmed && link != null && olderAdult != null) {
            onConfirmed(link.olderAdultId, olderAdult.fullName)
        }
    }

    CareLinkScreen(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onSendRequest = viewModel::sendLinkRequest,
        onAcceptConsent = viewModel::acceptConsent,
        onRejectConsent = viewModel::rejectConsent,
        modifier = modifier,
    )
}

@Composable
fun CareLinkScreen(
    state: CareLinkUiState,
    onCodeChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    onAcceptConsent: () -> Unit,
    onRejectConsent: () -> Unit,
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
            text = "Vincula a tu familiar",
            style = MaterialTheme.typography.headlineMedium,
            color = TataText,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "La relación de cuidado requiere consentimiento.",
            style = MaterialTheme.typography.bodySmall,
            color = TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )

        LinkProgress(
            step = state.step,
            modifier = Modifier.padding(top = 24.dp),
        )

        Spacer(Modifier.height(18.dp))
        TataFormField(
            label = "Código temporal",
            value = state.code,
            onValueChange = onCodeChange,
            placeholder = "TATA-4821",
            enabled = state.step == CareLinkStep.Code && !state.isLoading,
        )

        state.olderAdult?.let { profile ->
            Spacer(Modifier.height(14.dp))
            OlderAdultCard(profile, state.step)
        }

        Spacer(Modifier.height(14.dp))
        when (state.step) {
            CareLinkStep.Code -> TataButton(
                text = if (state.isLoading) "Enviando..." else "Enviar solicitud",
                enabled = !state.isLoading,
                onClick = onSendRequest,
            )

            CareLinkStep.AwaitingConsent -> ConsentCard(
                profile = state.olderAdult,
                isLoading = state.isLoading,
                onAccept = onAcceptConsent,
                onReject = onRejectConsent,
            )

            CareLinkStep.Confirmed -> StatusCard(
                title = "Vínculo confirmado",
                message = "El consentimiento fue registrado y el seguimiento ya está autorizado.",
                containerColor = TataMint,
            )

            CareLinkStep.Rejected -> StatusCard(
                title = "Seguimiento restringido",
                message = "El adulto mayor no autorizó el vínculo. Su información permanece protegida.",
                containerColor = TataErrorSurface,
            )
        }

        Spacer(Modifier.height(14.dp))
        TataCard(containerColor = TataMint) {
            Text(
                text = "Vínculo seguro y reversible",
                color = TataDeepNavy,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when (state.step) {
                    CareLinkStep.Code -> "El adulto mayor decide si autoriza el seguimiento."
                    CareLinkStep.AwaitingConsent -> "El seguimiento sigue restringido hasta registrar una decisión."
                    CareLinkStep.Confirmed -> "El adulto mayor puede retirar su consentimiento posteriormente."
                    CareLinkStep.Rejected -> "Sin consentimiento no se habilita el acceso de seguimiento."
                },
                color = TataMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        state.errorMessage?.let { message ->
            Spacer(Modifier.height(14.dp))
            TataCard(containerColor = TataErrorSurface) {
                Text(
                    text = message,
                    color = TataError,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun LinkProgress(
    step: CareLinkStep,
    modifier: Modifier = Modifier,
) {
    val requestReached = step != CareLinkStep.Code
    val consentReached = step == CareLinkStep.Confirmed || step == CareLinkStep.Rejected

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TataLavender, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ProgressItem("1", "Código", active = true)
        ProgressItem("2", "Solicitud", active = requestReached)
        ProgressItem("3", "Consentimiento", active = consentReached)
    }
}

@Composable
private fun ProgressItem(
    number: String,
    label: String,
    active: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            color = if (active) TataDeepNavy else TataMuted,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = if (active) TataDeepNavy else TataMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun OlderAdultCard(
    profile: OlderAdultProfile,
    step: CareLinkStep,
) {
    TataCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(TataLavender)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = profile.fullName.take(1).uppercase(),
                    color = TataDeepNavy,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column {
                Text(
                    text = profile.fullName,
                    color = TataText,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = ageLabel(profile.birthDate),
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = when (step) {
                        CareLinkStep.Code -> "Perfil encontrado"
                        CareLinkStep.AwaitingConsent -> "Solicitud lista para consentimiento"
                        CareLinkStep.Confirmed -> "Vínculo activo"
                        CareLinkStep.Rejected -> "Vínculo no autorizado"
                    },
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ConsentCard(
    profile: OlderAdultProfile?,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    TataCard(containerColor = TataLavender) {
        Text(
            text = "Consentimiento del adulto mayor",
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (profile != null) {
                profile.fullName + " autoriza el acceso a adherencia, alertas e información necesaria para su seguimiento."
            } else {
                "El adulto mayor autoriza el acceso necesario para el seguimiento."
            },
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onAccept,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TataNavy,
                    contentColor = Color.White,
                ),
            ) {
                Text(if (isLoading) "Procesando..." else "Aceptar vínculo")
            }
            OutlinedButton(
                onClick = onReject,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, TataBorder),
            ) {
                Text("Rechazar", color = TataDeepNavy)
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    message: String,
    containerColor: Color,
) {
    TataCard(containerColor = containerColor) {
        Text(
            text = title,
            color = TataDeepNavy,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

private fun ageLabel(birthDate: LocalDate): String {
    val years = Period.between(birthDate, LocalDate.now()).years.coerceAtLeast(0)
    return years.toString() + " años"
}
