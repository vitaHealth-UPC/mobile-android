package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.R
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import com.vitahealth.tata.shared.design.components.CaregiverTab
import com.vitahealth.tata.shared.design.components.CaregiverTabBar
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.Instant

@Composable
fun AlertsRoute(
    factory: AlertsViewModel.Factory,
    onOpenAlert: (Long) -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    val model: AlertsViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) model.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    AlertsScreen(
        state = state,
        onRetry = model::refresh,
        onFilterSelected = model::selectFilter,
        onOpenAlert = onOpenAlert,
        onTabSelected = onTabSelected,
    )
}

@Composable
fun AlertsScreen(
    state: AlertsUiState,
    onRetry: () -> Unit = {},
    onFilterSelected: (AlertFilter) -> Unit = {},
    onOpenAlert: (Long) -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
) {
    val formatter = rememberAlertDateFormatter()
    ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp)) {
    Column(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AlertsTitle(stringResource(R.string.alerts_title), Modifier.weight(1f))
                    TataSvgIcon(R.raw.alerts_bell, Modifier.size(22.dp))
                }
                Text(
                    text = stringResource(R.string.alerts_subtitle),
                    color = AlertsSecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            when (state) {
                AlertsUiState.Loading -> item { AlertsLoading(stringResource(R.string.alerts_loading)) }
                AlertsUiState.Empty -> item { NoAlertsCard() }
                is AlertsUiState.Error -> item {
                    AlertsProblemCard(state.problem, R.string.alerts_error_list_not_found, onRetry)
                }
                is AlertsUiState.Content -> {
                    item {
                        Text(
                            text = pluralStringResource(R.plurals.alerts_pending_summary, state.pendingCount, state.pendingCount),
                            color = tataTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        AlertFilters(selected = state.filter, onSelected = onFilterSelected)
                    }
                    if (state.visibleAlerts.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.alerts_filter_empty),
                                color = AlertsSecondaryText,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                    items(state.visibleAlerts, key = { it.id }) { alert ->
                        AlertListItem(alert, formatter.format(alert.scheduledAt), onClick = { onOpenAlert(alert.id) })
                    }
                }
            }
        }
        CaregiverTabBar(selected = CaregiverTab.Alerts, onSelect = onTabSelected)
    }
    }
}

@Composable
private fun NoAlertsCard() {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = TataMint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlertMark(AlertStatus.ATTENDED)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = stringResource(R.string.alerts_empty_title),
                    color = tataTextColor(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.alerts_empty_message),
                    color = AlertsSecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AlertFilters(selected: AlertFilter, onSelected: (AlertFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlertFilter.entries.forEach { filter ->
            val label = when (filter) {
                AlertFilter.ALL -> R.string.alerts_filter_all
                AlertFilter.PENDING -> R.string.alerts_filter_pending
                AlertFilter.ATTENDED -> R.string.alerts_filter_attended
            }
            // Material filter chips keep a 48 dp touch target around their 32 dp body.
            FilterChip(
                selected = filter == selected,
                onClick = { onSelected(filter) },
                label = { Text(stringResource(label)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TataNavy,
                    selectedLabelColor = Color.White,
                    containerColor = TataLavender,
                    labelColor = TataNavy,
                ),
            )
        }
    }
}

@Composable
private fun AlertListItem(alert: CaregiverAlert, scheduled: String, onClick: () -> Unit) {
    val openLabel = stringResource(R.string.alerts_open_detail)
    val colors = if (alert.status == AlertStatus.OPEN) listOf(Color(0xFFFFF0E8), Color(0xFFFBE0D1))
        else listOf(Color(0xFFEAF5FB), Color(0xFFDDECF7))
    Row(
        Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
            .clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(colors))
            .clickable(role = Role.Button, onClickLabel = openLabel, onClick = onClick)
            .heightIn(min = 106.dp).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlertMark(alert.status)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(stringResource(alert.status.titleRes()), color = tataTextColor(), fontSize = 15.sp,
                lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(stringResource(R.string.alerts_card_scheduled, scheduled), color = AlertsSecondaryText, fontSize = 11.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(7.dp))
            Text(alert.medicationName, color = tataTextColor(), fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
            if (alert.reason.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(alert.reason, color = AlertsSecondaryText, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (alert.status != AlertStatus.OPEN) {
                Spacer(Modifier.height(6.dp))
                AlertStatusChip(alert.status)
            }
        }
        Text("›", color = TataNavy, fontSize = 20.sp, lineHeight = 24.sp)
    }
}

internal val previewAlerts = listOf(
    CaregiverAlert(
        id = 1, intakeId = "101", medicationName = "Losartán 50 mg",
        scheduledAt = Instant.parse("2026-10-05T13:00:00Z"), reason = "Toma no confirmada dentro del periodo de tolerancia",
        status = AlertStatus.OPEN, openedAt = Instant.parse("2026-10-05T13:30:00Z"), closedAt = null,
    ),
    CaregiverAlert(
        id = 2, intakeId = "98", medicationName = "Metformina 500 mg",
        scheduledAt = Instant.parse("2026-10-04T17:30:00Z"), reason = "Toma no confirmada dentro del periodo de tolerancia",
        status = AlertStatus.ATTENDED, openedAt = Instant.parse("2026-10-04T18:00:00Z"), closedAt = null,
    ),
)

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
