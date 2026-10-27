package com.vitahealth.tata.monitoring.presentation.alerts.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertEvidenceTimeline
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertFollowUpActions
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertInfoRow
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertPersonCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertStatusChip
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsBackButton
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsLoading
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsProblemCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.MedicationCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.alertsSerif
import com.vitahealth.tata.monitoring.presentation.alerts.ui.labelRes
import com.vitahealth.tata.monitoring.presentation.alerts.ui.rememberAlertDateFormatter
import com.vitahealth.tata.monitoring.presentation.alerts.ui.titleRes
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFeedback
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataInterFontFamily
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataInterTypography
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import java.time.Instant

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
    person: MonitoredPerson? = null,
    history: List<RecordedAlertEvent>? = null,
    onOpenProfile: () -> Unit = {},
) {
    val formatter = rememberAlertDateFormatter()
    TataInterTypography {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = TataInterFontFamily,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        ) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                            if (LocalTataAccessibility.current.darkTheme) {
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background,
                                )
                            } else {
                                listOf(Color.White, TataSurface, Color(0xFFFCFBFF))
                            }
                        )
                        )
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    val alert = (state as? AlertDetailUiState.Content)?.alert
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AlertsBackButton(onBack)
                        Text(
                            text =
                                stringResource(
                                    alert?.status?.titleRes() ?: R.string.alert_detail_title
                                ),
                            fontFamily = alertsSerif,
                            fontSize = 24.sp,
                            lineHeight = 27.sp,
                            color = tataTextColor(),
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                        )
                        if (alert != null) AlertStatusChip(alert.status)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    when (state) {
                        AlertDetailUiState.Loading ->
                            AlertsLoading(stringResource(R.string.alert_detail_loading))
                        is AlertDetailUiState.Error ->
                            AlertsProblemCard(
                                state.problem,
                                R.string.alerts_error_detail_not_found,
                                onRetry,
                            )
                        is AlertDetailUiState.Content -> {
                            MedicationCard(
                                state.alert,
                                formatter.format(state.alert.scheduledAt),
                                state.dose?.dose,
                            )
                            person?.let {
                                Spacer(Modifier.height(20.dp))
                                AlertPersonCard(it, onOpenProfile)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            AlertEvidenceTimeline(state.alert, formatter, history)
                            AlertFollowUpActions(
                                state,
                                onUpdateStatus,
                                Modifier.padding(top = 12.dp),
                                noteSaved,
                                onAddNote,
                                contact,
                                onRetryContact,
                            )
                            var showDetails by
                                rememberSaveable(state.alert.id) { mutableStateOf(false) }
                            TextButton(onClick = { showDetails = !showDetails }) {
                                Text(stringResource(R.string.alert_detail_section), fontSize = 12.sp)
                            }
                            if (showDetails) {
                                Text(
                                    text = stringResource(R.string.alert_detail_section),
                                    color = tataTextColor(),
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TataCard(modifier = Modifier.fillMaxWidth()) {
                                    AlertInfoRow(
                                        stringResource(R.string.alert_detail_reason),
                                        state.alert.reason.ifBlank {
                                            stringResource(R.string.alert_detail_reason_missing)
                                        },
                                    )
                                    AlertInfoRow(
                                        stringResource(R.string.alert_detail_status),
                                        stringResource(state.alert.status.labelRes()),
                                    )
                                    AlertInfoRow(
                                        stringResource(R.string.alert_detail_opened),
                                        formatter.format(state.alert.openedAt),
                                    )
                                    state.alert.closedAt?.let {
                                        AlertInfoRow(
                                            stringResource(R.string.alert_detail_closed),
                                            formatter.format(it),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                CaregiverTabBar(selected = CaregiverTab.Alerts, onSelect = onTabSelected)
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
    val closed =
        previewAlerts
            .first()
            .copy(status = AlertStatus.CLOSED, closedAt = Instant.parse("2026-10-05T15:10:00Z"))
    TataTheme { AlertDetailScreen(state = AlertDetailUiState.Content(closed)) }
}

@Preview(name = "Detalle atendida con aviso", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailAttendedPreview() {
    val attended = previewAlerts.first().copy(status = AlertStatus.ATTENDED)
    TataTheme {
        AlertDetailScreen(
            state =
                AlertDetailUiState.Content(
                    attended,
                    feedback = AlertFeedback.StatusChanged(AlertStatus.ATTENDED),
                )
        )
    }
}

@Preview(name = "Detalle con conflicto (409)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertDetailConflictPreview() {
    TataTheme {
        AlertDetailScreen(
            state =
                AlertDetailUiState.Content(
                    previewAlerts.first(),
                    feedback = AlertFeedback.Failed(AlertsProblem.CONFLICT),
                )
        )
    }
}

@Preview(name = "Detalle con contacto", showBackground = true, widthDp = 393, heightDp = 1100)
@Composable
private fun AlertDetailContactPreview() {
    val option =
        ContactOption(
            ContactChannel(ContactChannelType.PHONE, "+51 999 888 777"),
            firstName = "Rosa",
        )
    TataTheme {
        AlertDetailScreen(
            state = AlertDetailUiState.Content(previewAlerts.first()),
            contact = ContactUiState.Ready(option),
        )
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
