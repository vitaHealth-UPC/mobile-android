package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.ContactChannelType
import com.vitahealth.tata.monitoring.domain.model.ContactOption
import com.vitahealth.tata.monitoring.presentation.notes.NoteComposerDialog
import com.vitahealth.tata.shared.design.components.CaregiverTab
import com.vitahealth.tata.shared.design.components.CaregiverTabBar
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataTextColor

@Composable
fun AlertDetailRoute(
    factory: AlertDetailViewModel.Factory,
    onBack: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    val model: AlertDetailViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val composer by model.noteComposer.state.collectAsState()
    val contact by model.contact.collectAsState()
    AlertDetailScreen(
        state, onBack, model::load, onTabSelected, model::updateStatus, composer.saved, model.noteComposer::open,
        contact, model::loadContact,
    )
    NoteComposerDialog(composer, onSave = { model.noteComposer.save(it) }, onDismiss = model.noteComposer::dismiss)
}

@Composable
fun AlertDetailScreen(
    state: AlertDetailUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    onUpdateStatus: (AlertStatus) -> Unit = {},
    noteSaved: Boolean = false,
    onAddNote: () -> Unit = {},
    contact: ContactUiState = ContactUiState.Loading,
    onRetryContact: () -> Unit = {},
) {
    val formatter = rememberAlertDateFormatter()
    ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp)) {
    Column(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            val alert = (state as? AlertDetailUiState.Content)?.alert
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlertsBackButton(onBack)
                Text(
                    text = stringResource(alert?.status?.titleRes() ?: R.string.alert_detail_title),
                    fontFamily = alertsSerif, fontSize = 22.sp, lineHeight = 27.sp, color = tataTextColor(),
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                if (alert != null) AlertStatusChip(alert.status)
            }
            Spacer(modifier = Modifier.height(20.dp))
            when (state) {
                AlertDetailUiState.Loading -> AlertsLoading(stringResource(R.string.alert_detail_loading))
                is AlertDetailUiState.Error ->
                    AlertsProblemCard(state.problem, R.string.alerts_error_detail_not_found, onRetry)
                is AlertDetailUiState.Content -> {
                    MedicationCard(state.alert, formatter.format(state.alert.scheduledAt))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.alert_detail_section),
                        color = tataTextColor(),
                        fontSize = 14.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TataCard(modifier = Modifier.fillMaxWidth()) {
                        AlertInfoRow(
                            stringResource(R.string.alert_detail_reason),
                            state.alert.reason.ifBlank { stringResource(R.string.alert_detail_reason_missing) },
                        )
                        AlertInfoRow(stringResource(R.string.alert_detail_status), stringResource(state.alert.status.labelRes()))
                        AlertInfoRow(stringResource(R.string.alert_detail_opened), formatter.format(state.alert.openedAt))
                        state.alert.closedAt?.let {
                            AlertInfoRow(stringResource(R.string.alert_detail_closed), formatter.format(it))
                        }
                    }
                    AlertFollowUpActions(state, onUpdateStatus, Modifier.padding(top = 20.dp), noteSaved, onAddNote, contact, onRetryContact)
                }
            }
        }
        CaregiverTabBar(selected = CaregiverTab.Alerts, onSelect = onTabSelected)
    }
    }
}

@Composable
private fun MedicationCard(alert: CaregiverAlert, scheduled: String) {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = alert.status.surface()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlertMark(alert.status)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = alert.medicationName,
                    color = tataTextColor(),
                    fontSize = 18.sp, lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.alert_detail_scheduled),
                    color = AlertsSecondaryText,
                    fontSize = 11.sp, lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = scheduled,
                    color = tataTextColor(),
                    fontSize = 14.sp, lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Image(painterResource(R.drawable.alert_tablet), null, Modifier.size(70.dp))
        }
    }
}

@Preview(name = "Detalle pendiente", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailOpenPreview() {
    TataTheme { AlertDetailScreen(state = AlertDetailUiState.Content(previewAlerts.first())) }
}

@Preview(name = "Detalle cerrada", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailClosedPreview() {
    val closed = previewAlerts.first().copy(
        status = AlertStatus.CLOSED,
        closedAt = java.time.Instant.parse("2026-10-05T15:10:00Z"),
    )
    TataTheme { AlertDetailScreen(state = AlertDetailUiState.Content(closed)) }
}

@Preview(name = "Detalle atendida con aviso", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailAttendedPreview() {
    val attended = previewAlerts.first().copy(status = AlertStatus.ATTENDED)
    TataTheme {
        AlertDetailScreen(state = AlertDetailUiState.Content(attended, feedback = AlertFeedback.StatusChanged(AlertStatus.ATTENDED)))
    }
}

@Preview(name = "Detalle con conflicto (409)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailConflictPreview() {
    TataTheme {
        AlertDetailScreen(state = AlertDetailUiState.Content(previewAlerts.first(), feedback = AlertFeedback.Failed(AlertsProblem.CONFLICT)))
    }
}

@Preview(name = "Detalle con contacto", showBackground = true, widthDp = 393, heightDp = 1100)
@Composable
private fun AlertDetailContactPreview() {
    val option = ContactOption(ContactChannel(ContactChannelType.PHONE, "+51 999 888 777"), firstName = "Rosa")
    TataTheme {
        AlertDetailScreen(state = AlertDetailUiState.Content(previewAlerts.first()), contact = ContactUiState.Ready(option))
    }
}

@Preview(name = "Contacto no disponible", showBackground = true, widthDp = 393, heightDp = 1100)
@Composable
private fun AlertDetailNoContactPreview() {
    TataTheme {
        AlertDetailScreen(
            state = AlertDetailUiState.Content(previewAlerts.first()),
            contact = ContactUiState.Ready(ContactOption(channel = null, firstName = "Rosa")),
        )
    }
}

@Preview(name = "Alerta no encontrada (404)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailNotFoundPreview() {
    TataTheme { AlertDetailScreen(state = AlertDetailUiState.Error(AlertsProblem.NOT_FOUND)) }
}

@Preview(name = "Cargando detalle", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailLoadingPreview() {
    TataTheme { AlertDetailScreen(state = AlertDetailUiState.Loading) }
}
