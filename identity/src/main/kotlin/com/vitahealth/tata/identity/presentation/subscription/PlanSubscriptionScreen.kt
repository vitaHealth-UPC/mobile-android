package com.vitahealth.tata.identity.presentation.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.R
import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.PlanCapability
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.identity.domain.model.SubscriptionStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataBlueSurface
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val PlanFont = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter))

private val StatusGreen = androidx.compose.ui.graphics.Color(0xFF296345)

@Composable
fun PlanSubscriptionRoute(
    factory: PlanSubscriptionViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlanSubscriptionViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    PlanSubscriptionScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onSelectPlan = viewModel::onSelectPlan,
        onChangePlan = viewModel::onChangeRequest,
        onChangeCancel = viewModel::onChangeCancel,
        onChangeConfirm = viewModel::onChangeConfirm,
        modifier = modifier,
    )
}

@Composable
fun PlanSubscriptionScreen(
    state: PlanSubscriptionUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectPlan: (Plan) -> Unit,
    onChangePlan: () -> Unit,
    onChangeCancel: () -> Unit,
    onChangeConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Header(onBack = onBack)
        Spacer(Modifier.height(16.dp))

        val subscription = state.subscription
        when {
            state.isLoading -> TataCard(Modifier.fillMaxWidth(), TataMint) {
                Text(stringResource(R.string.plan_loading), color = TataText)
            }

            subscription == null -> {
                state.message?.let { ErrorBanner(it) }
                Spacer(Modifier.height(12.dp))
                TataButton(text = stringResource(R.string.plan_retry), onClick = onRetry)
            }

            else -> {
                CurrentPlanCard(subscription)
                Spacer(Modifier.height(16.dp))
                state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isCurrent = subscription.isOn(plan),
                        isSelected = state.selectedPlan?.code == plan.code,
                        enabled = !state.isChanging,
                        onSelect = { onSelectPlan(plan) },
                    )
                    Spacer(Modifier.height(14.dp))
                }
                TataButton(
                    text = stringResource(R.string.plan_change_button),
                    onClick = onChangePlan,
                    enabled = state.selectedPlan != null && !state.isChanging,
                )
                if (state.selectedPlan == null) {
                    Text(
                        text = stringResource(R.string.plan_select_hint),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
                        color = TataMuted,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                RenewalCard(subscription)
                state.confirming?.let { target ->
                    ChangeDialog(subscription, target, onChangeCancel, onChangeConfirm)
                }
                state.changeMessage?.let {
                    Spacer(Modifier.height(16.dp))
                    ChangeBanner(it, state.changeMessageIsError)
                }
            }
        }
    }
}

@Composable
private fun ChangeDialog(
    subscription: Subscription,
    target: Plan,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val impact = subscription.impactOfChangingTo(target)
    fun names(capabilities: Set<PlanCapability>) =
        capabilities.sortedBy { it.ordinal }.joinToString(", ") { context.getString(capabilityLabel(it)) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.plan_confirm_title, target.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.plan_price_per_month, formatPlanPrice(target.monthlyPrice, target.currency)),
                    fontWeight = FontWeight.SemiBold,
                )
                if (impact.gained.isNotEmpty()) {
                    Text(stringResource(R.string.plan_confirm_gain, names(impact.gained)))
                }
                if (impact.lost.isNotEmpty()) {
                    Text(stringResource(R.string.plan_confirm_lose, names(impact.lost)))
                }
                Text(stringResource(R.string.plan_renewal_note))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.plan_confirm_action)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.plan_cancel)) }
        },
    )
}

@Composable
private fun ChangeBanner(message: PlanChangeMessage, isError: Boolean) {
    val body = stringResource(
        when (message) {
            PlanChangeMessage.Updated -> R.string.plan_updated_body
            PlanChangeMessage.ErrorPlanUnavailable -> R.string.plan_change_error_plan
            PlanChangeMessage.ErrorAccount -> R.string.plan_error_account
            PlanChangeMessage.ErrorSession -> R.string.plan_error_session
            PlanChangeMessage.ErrorOffline -> R.string.plan_error_offline
            PlanChangeMessage.ErrorGeneric -> R.string.plan_error_generic
        },
    )
    TataCard(Modifier.fillMaxWidth(), if (isError) TataErrorSurface else TataMint) {
        Text(
            text = stringResource(if (isError) R.string.plan_change_error_title else R.string.plan_updated_title),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            fontWeight = FontWeight.SemiBold,
            color = if (isError) TataError else StatusGreen,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            color = if (isError) TataError else TataMuted,
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val backLabel = stringResource(R.string.plan_back)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = backLabel },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "‹", fontSize = 28.sp, color = TataNavy)
        }
        Column {
            Text(
                text = stringResource(R.string.plan_title),
                fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
                fontSize = 26.sp,
                color = TataText,
            )
            Text(
                text = stringResource(R.string.plan_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
                color = TataMuted,
            )
        }
    }
}

@Composable
private fun CurrentPlanCard(subscription: Subscription) {
    TataCard(Modifier.fillMaxWidth(), TataMint) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plan_header, subscription.plan.name),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp, fontFamily = PlanFont),
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.plan_price_per_month,
                    formatPlanPrice(subscription.plan.monthlyPrice, subscription.plan.currency),
                ),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp, fontFamily = PlanFont),
                fontWeight = FontWeight.Bold,
                color = TataNavy,
            )
        }
        val locale = LocalConfiguration.current.locales[0]
        val renews = subscription.renewsAt?.let { formatShortDate(it, locale) }
        Text(
            text = if (renews != null) {
                stringResource(R.string.plan_active_renews, renews)
            } else {
                stringResource(R.string.plan_status_active)
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(statusLabel(subscription.status)),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            fontWeight = FontWeight.SemiBold,
            color = StatusGreen,
            modifier = Modifier
                .padding(top = 10.dp)
                .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 4.dp),
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun PlanCard(
    plan: Plan,
    isCurrent: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val container = when {
        isCurrent -> TataLavender
        isSelected -> TataBlueSurface
        else -> androidx.compose.ui.graphics.Color.White
    }
    TataCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.border(BorderStroke(2.dp, TataNavy), RoundedCornerShape(18.dp)) else Modifier)
            // The current plan cannot be chosen again; the others behave as one choice of a group.
            .selectable(
                selected = isSelected,
                enabled = enabled && !isCurrent,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        containerColor = container,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp, fontFamily = PlanFont),
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (isCurrent) {
                    stringResource(R.string.plan_current_label)
                } else {
                    stringResource(R.string.plan_price_per_month, formatPlanPrice(plan.monthlyPrice, plan.currency))
                },
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp, fontFamily = PlanFont),
                fontWeight = FontWeight.Bold,
                color = TataNavy,
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            plan.capabilities.sortedBy { it.ordinal }.forEach { capability ->
                Text(
                    text = "✓ " + stringResource(capabilityLabel(capability)),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
                    color = TataText,
                )
            }
        }
    }
}

@Composable
private fun RenewalCard(subscription: Subscription) {
    val locale = LocalConfiguration.current.locales[0]
    TataCard(Modifier.fillMaxWidth(), TataBlueSurface) {
        Text(
            text = stringResource(R.string.plan_renewal_title),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = subscription.renewsAt?.let { formatLongDate(it, locale) } ?: stringResource(R.string.plan_renewal_unknown),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 23.sp, fontFamily = PlanFont),
            fontWeight = FontWeight.Bold,
            color = TataText,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.plan_renewal_note),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ErrorBanner(message: PlanMessage) {
    val body = stringResource(
        when (message) {
            PlanMessage.ErrorOffline -> R.string.plan_error_offline
            PlanMessage.ErrorAccount -> R.string.plan_error_account
            PlanMessage.ErrorSession -> R.string.plan_error_session
            PlanMessage.ErrorGeneric -> R.string.plan_error_generic
        },
    )
    TataCard(Modifier.fillMaxWidth(), TataErrorSurface) {
        Text(
            text = stringResource(R.string.plan_error_title),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont),
            fontWeight = FontWeight.SemiBold,
            color = TataError,
        )
        Text(text = body, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp, fontFamily = PlanFont), color = TataError)
    }
}

private fun statusLabel(status: SubscriptionStatus): Int =
    when (status) {
        SubscriptionStatus.ACTIVE -> R.string.plan_status_active
    }

private fun capabilityLabel(capability: PlanCapability): Int =
    when (capability) {
        PlanCapability.REMINDERS -> R.string.plan_capability_reminders
        PlanCapability.AGENDA -> R.string.plan_capability_agenda
        PlanCapability.INTAKE_CONFIRMATION -> R.string.plan_capability_confirmation
        PlanCapability.FAMILY_ALERTS -> R.string.plan_capability_alerts
        PlanCapability.FAMILY_MONITORING -> R.string.plan_capability_monitoring
        PlanCapability.ADHERENCE_INSIGHTS -> R.string.plan_capability_insights
    }

private fun formatShortDate(instant: Instant, locale: java.util.Locale): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(instant.atZone(ZoneId.systemDefault()))

private fun formatLongDate(instant: Instant, locale: java.util.Locale): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale).format(instant.atZone(ZoneId.systemDefault()))

@Preview(showBackground = true)
@Composable
private fun PlanSubscriptionScreenPreview() {
    val essential = Plan(
        "ESSENTIAL", "Esencial", BigDecimal("9.90"), "PEN",
        setOf(PlanCapability.REMINDERS, PlanCapability.AGENDA, PlanCapability.INTAKE_CONFIRMATION),
    )
    val family = Plan(
        "FAMILY", "Familiar", BigDecimal("19.90"), "PEN",
        PlanCapability.entries.toSet(),
    )
    TataTheme {
        PlanSubscriptionScreen(
            state = PlanSubscriptionUiState(
                isLoading = false,
                subscription = Subscription("acc", family, SubscriptionStatus.ACTIVE, Instant.parse("2026-10-28T00:00:00Z")),
                plans = listOf(essential, family),
            ),
            onBack = {},
            onRetry = {},
            onSelectPlan = {},
            onChangePlan = {},
            onChangeCancel = {},
            onChangeConfirm = {},
        )
    }
}
