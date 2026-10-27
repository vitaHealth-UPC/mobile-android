package com.vitahealth.tata.monitoring.presentation.alerts.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertDoseCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertFilters
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertListItem
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertTimelineSummary
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsLoading
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsProblemCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsSecondaryText
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsTitle
import com.vitahealth.tata.monitoring.presentation.alerts.ui.NoAlertsCard
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AlertsScreen(
    state: AlertsUiState,
    onRetry: () -> Unit = {},
    onFilterSelected: (AlertFilter) -> Unit = {},
    onOpenAlert: (Long) -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    onOpenDoseSummary: () -> Unit = {},
) {
    val timeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(LocalConfiguration.current.locales[0])
            .withZone(java.time.ZoneId.systemDefault())
    ProvideTextStyle(
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily(Font(R.font.tata_inter)),
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
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AlertsTitle(stringResource(R.string.alerts_title), Modifier.weight(1f))
                        TataSvgIcon(
                            R.raw.alerts_bell,
                            Modifier.size(22.dp),
                            colorFilter =
                                if (LocalTataAccessibility.current.darkTheme)
                                    ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                else null,
                        )
                    }
                    Text(
                        text =
                            if (state is AlertsUiState.Content)
                                state.day.format(
                                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                                        .withLocale(LocalConfiguration.current.locales[0])
                                )
                            else stringResource(R.string.alerts_subtitle),
                        color = AlertsSecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                    )
                }
                when (state) {
                    AlertsUiState.Loading ->
                        item { AlertsLoading(stringResource(R.string.alerts_loading)) }
                    AlertsUiState.Empty -> item { NoAlertsCard() }
                    is AlertsUiState.Error ->
                        item {
                            AlertsProblemCard(
                                state.problem,
                                R.string.alerts_error_list_not_found,
                                onRetry,
                            )
                        }
                    is AlertsUiState.Content -> {
                        item {
                            AlertTimelineSummary(state.pendingCount)
                            Spacer(Modifier.height(10.dp))
                            AlertFilters(selected = state.filter, onSelected = onFilterSelected)
                        }
                        if (state.visibleAlerts.isEmpty() && state.visibleDoses.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.alerts_filter_empty),
                                    color = AlertsSecondaryText,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                        items(state.visibleAlerts, key = { "alert-${it.id}" }) { alert ->
                            AlertListItem(
                                alert,
                                timeFormatter.format(alert.scheduledAt),
                                onClick = { onOpenAlert(alert.id) },
                                name = state.olderAdultName,
                            )
                        }
                        items(state.visibleDoses, key = { "dose-${it.id}" }) { dose ->
                            AlertDoseCard(
                                dose,
                                timeFormatter.format(dose.scheduledAt),
                                state.olderAdultName,
                                onOpen = onOpenDoseSummary,
                                remainingMinutes =
                                    java.time.Duration.between(state.asOf, dose.scheduledAt)
                                        .toMinutes()
                                        .coerceAtLeast(1),
                            )
                        }
                    }
                }
            }
            CaregiverTabBar(selected = CaregiverTab.Alerts, onSelect = onTabSelected)
        }
    }
}

@Preview(name = "Alertas con datos", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertsContentPreview() {
    TataTheme { AlertsScreen(state = AlertsUiState.Content(previewAlerts)) }
}

@Preview(name = "Sin alertas", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertsEmptyPreview() {
    TataTheme { AlertsScreen(state = AlertsUiState.Empty) }
}

@Preview(name = "Error de red", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertsErrorPreview() {
    TataTheme { AlertsScreen(state = AlertsUiState.Error(AlertsProblem.NETWORK)) }
}

@Preview(name = "Sin vínculo (403)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertsAccessDeniedPreview() {
    TataTheme { AlertsScreen(state = AlertsUiState.Error(AlertsProblem.ACCESS_DENIED)) }
}

@Preview(name = "Cargando", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AlertsLoadingPreview() {
    TataTheme { AlertsScreen(state = AlertsUiState.Loading) }
}
