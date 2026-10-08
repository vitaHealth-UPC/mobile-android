package com.vitahealth.tata.monitoring.presentation.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.ContactChannelType
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsSecondaryText
import com.vitahealth.tata.monitoring.presentation.alerts.ContactUiState
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataHighContrastMuted
import com.vitahealth.tata.shared.design.theme.TataNavy

/**
 * "Contactar a <nombre>" of the alert follow-up. With no channel it shows the grey "Contacto no disponible"
 * of the Figma; a failed lookup also offers to ask again.
 */
@Composable
internal fun ContactAction(contact: ContactUiState, onRetry: () -> Unit) {
    val context = LocalContext.current
    var launchFailed by rememberSaveable { mutableStateOf(false) }
    Column {
        when (contact) {
            ContactUiState.Loading -> UnavailableContact(stringResource(R.string.contact_loading))
            is ContactUiState.Ready -> {
                val channel = contact.option.channel
                if (channel == null) {
                    UnavailableContact(stringResource(R.string.contact_unavailable))
                } else {
                    val name = contact.option.firstName
                    TataButton(
                        text = if (name != null) stringResource(R.string.contact_action_named, name) else stringResource(R.string.contact_action),
                        onClick = { launchFailed = !context.openContact(channel) },
                    )
                    Text(
                        text = channelLabel(channel),
                        color = AlertsSecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp, start = 8.dp),
                    )
                }
            }
            is ContactUiState.Failed -> {
                UnavailableContact(stringResource(R.string.contact_unavailable))
                if (contact.problem == AlertsProblem.NETWORK || contact.problem == AlertsProblem.UNKNOWN) {
                    TextButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(stringResource(R.string.contact_retry), color = TataNavy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (launchFailed) {
            Text(
                text = stringResource(R.string.contact_no_app),
                color = TataError,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun channelLabel(channel: ContactChannel): String = when (channel.type) {
    ContactChannelType.PHONE -> stringResource(R.string.contact_channel_phone, channel.value)
    ContactChannelType.WHATSAPP -> stringResource(R.string.contact_channel_whatsapp, channel.value)
}

/** Grey, non-clickable pill of the Figma "Contacto no disponible"; its text keeps 7:1 contrast anyway. */
@Composable
private fun UnavailableContact(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TataBorder, RoundedCornerShape(28.dp))
            .semantics { disabled() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = TataHighContrastMuted, fontWeight = FontWeight.SemiBold)
    }
}
