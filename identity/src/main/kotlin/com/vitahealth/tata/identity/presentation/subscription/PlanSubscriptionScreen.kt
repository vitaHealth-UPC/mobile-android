package com.vitahealth.tata.identity.presentation.subscription

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = modifier,
    )
}

@Composable
fun PlanSubscriptionScreen(
    state: PlanSubscriptionUiState,
    onBack: () -> Unit,
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
                    PlanCard(plan = plan, isCurrent = subscription.isOn(plan))
                    Spacer(Modifier.height(14.dp))
                }
                RenewalCard(subscription)
            }
        }
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
                style = MaterialTheme.typography.bodySmall,
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.plan_price_per_month,
                    formatPlanPrice(subscription.plan.monthlyPrice, subscription.plan.currency),
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TataNavy,
            )
        }
        val renews = subscription.renewsAt?.let { formatShortDate(it) }
        Text(
            text = if (renews != null) {
                stringResource(R.string.plan_active_renews, renews)
            } else {
                stringResource(R.string.plan_status_active)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(statusLabel(subscription.status)),
            style = MaterialTheme.typography.labelLarge,
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
private fun PlanCard(plan: Plan, isCurrent: Boolean) {
    TataCard(Modifier.fillMaxWidth(), if (isCurrent) TataLavender else androidx.compose.ui.graphics.Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
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
                style = MaterialTheme.typography.titleSmall,
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
                    style = MaterialTheme.typography.labelMedium,
                    color = TataText,
                )
            }
        }
    }
}

@Composable
private fun RenewalCard(subscription: Subscription) {
    TataCard(Modifier.fillMaxWidth(), TataBlueSurface) {
        Text(
            text = stringResource(R.string.plan_renewal_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = subscription.renewsAt?.let { formatLongDate(it) } ?: stringResource(R.string.plan_renewal_unknown),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TataText,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.plan_renewal_note),
            style = MaterialTheme.typography.bodySmall,
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TataError,
        )
        Text(text = body, style = MaterialTheme.typography.bodySmall, color = TataError)
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

private fun formatShortDate(instant: Instant): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(instant.atZone(ZoneId.systemDefault()))

private fun formatLongDate(instant: Instant): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(instant.atZone(ZoneId.systemDefault()))

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
        )
    }
}
