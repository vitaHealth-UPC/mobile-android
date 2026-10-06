package com.vitahealth.tata.carelink.presentation.link

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.carelink.domain.model.OlderAdultProfile
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import java.time.LocalDate
import java.time.Period

@Composable
fun CareLinkRoute(
    factory: CareLinkViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: CareLinkViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    CareLinkScreen(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onSendRequest = viewModel::sendLinkRequest,
        modifier = modifier,
    )
}

@Composable
fun CareLinkScreen(
    state: CareLinkUiState,
    onCodeChange: (String) -> Unit,
    onSendRequest: () -> Unit,
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
            awaitingConsent = state.step == CareLinkStep.AwaitingConsent,
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
            OlderAdultCard(profile)
        }

        Spacer(Modifier.height(14.dp))
        if (state.step == CareLinkStep.Code) {
            TataButton(
                text = if (state.isLoading) "Enviando..." else "Enviar solicitud",
                enabled = !state.isLoading,
                onClick = onSendRequest,
            )
        } else {
            AwaitingConsentCard(state.olderAdult)
        }

        Spacer(Modifier.height(14.dp))
        TataCard(containerColor = TataMint) {
            Text(
                text = "Vínculo seguro y reversible",
                color = TataDeepNavy,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (state.step == CareLinkStep.Code) {
                    "El adulto mayor decide si autoriza el seguimiento."
                } else {
                    "Solicitud registrada. El seguimiento seguirá restringido hasta contar con consentimiento."
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
    awaitingConsent: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TataLavender, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ProgressItem("1", "Código", active = true)
        ProgressItem("2", "Solicitud", active = awaitingConsent)
        ProgressItem("3", "Consentimiento", active = false)
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
private fun OlderAdultCard(profile: OlderAdultProfile) {
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
                    text = "Solicitud lista para consentimiento",
                    color = TataMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun AwaitingConsentCard(profile: OlderAdultProfile?) {
    TataCard(containerColor = TataLavender) {
        Text(
            text = "Consentimiento del adulto mayor",
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (profile != null) {
                "${profile.fullName} debe autorizar el acceso a adherencia, alertas e información necesaria para su seguimiento."
            } else {
                "El adulto mayor debe autorizar el acceso necesario para el seguimiento."
            },
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Esperando consentimiento",
            color = TataDeepNavy,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

private fun ageLabel(birthDate: LocalDate): String {
    val years = Period.between(birthDate, LocalDate.now()).years.coerceAtLeast(0)
    return "$years años"
}
