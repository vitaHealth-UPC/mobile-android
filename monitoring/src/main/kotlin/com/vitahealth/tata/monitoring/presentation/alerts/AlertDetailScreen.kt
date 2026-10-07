package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.R
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.design.components.CaregiverTab
import com.vitahealth.tata.shared.design.components.CaregiverTabBar
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataTextColor

/**
 * [actions] is the follow-up slot of the alert (contact, note, attend). This screen only reads the alert,
 * so the slot stays empty until those actions exist.
 */
@Composable
fun AlertDetailRoute(
    factory: AlertDetailViewModel.Factory,
    onBack: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
    actions: @Composable ColumnScope.(CaregiverAlert) -> Unit = {},
) {
    val model: AlertDetailViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    AlertDetailScreen(state, onBack, model::load, onTabSelected, actions)
}

@Composable
fun AlertDetailScreen(
    state: AlertDetailUiState,
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    actions: @Composable ColumnScope.(CaregiverAlert) -> Unit = {},
) {
    val formatter = rememberAlertDateFormatter()
    Column(modifier = Modifier.fillMaxSize().background(TataSurface)) {
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
                AlertsTitle(
                    text = stringResource(alert?.status?.titleRes() ?: R.string.alert_detail_title),
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
                        style = MaterialTheme.typography.titleMedium,
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
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                        actions(state.alert)
                    }
                }
            }
        }
        CaregiverTabBar(selected = CaregiverTab.Alerts, onSelect = onTabSelected)
    }
}

@Composable
private fun MedicationCard(alert: CaregiverAlert, scheduled: String) {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = alert.status.surface()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlertMark(alert.status)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = alert.medicationName,
                    color = tataTextColor(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.alert_detail_scheduled),
                    color = AlertsSecondaryText,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = scheduled,
                    color = tataTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
