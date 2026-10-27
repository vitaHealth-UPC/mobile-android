package com.vitahealth.tata.identity.presentation.subscription.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import com.vitahealth.tata.identity.presentation.subscription.mappers.featuredPlanCapabilities
import com.vitahealth.tata.identity.presentation.subscription.mappers.formatPlanPrice
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanChangeMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanMessage
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBlueSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val PlanFont = FontFamily(Font(R.font.tata_inter))
internal val StatusGreen = Color(0xFF296345)

@Composable
internal fun ChangeDialog(
    subscription: Subscription,
    target: Plan,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val impact = subscription.impactOfChangingTo(target)
    fun names(capabilities: Set<PlanCapability>) =
        capabilities
            .sortedBy { it.ordinal }
            .joinToString(", ") { context.getString(capabilityLabel(it)) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.plan_confirm_title, target.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(
                        R.string.plan_price_per_month,
                        formatPlanPrice(target.monthlyPrice, target.currency),
                    ),
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
internal fun ChangeBanner(message: PlanChangeMessage, isError: Boolean) {
    val body =
        stringResource(
            when (message) {
                PlanChangeMessage.Updated -> R.string.plan_updated_body
                PlanChangeMessage.ErrorPlanUnavailable -> R.string.plan_change_error_plan
                PlanChangeMessage.ErrorAccount -> R.string.plan_error_account
                PlanChangeMessage.ErrorSession -> R.string.plan_error_session
                PlanChangeMessage.ErrorOffline -> R.string.plan_error_offline
                PlanChangeMessage.ErrorGeneric -> R.string.plan_error_generic
            }
        )
    TataCard(Modifier.fillMaxWidth(), if (isError) TataErrorSurface else TataMint) {
        Text(
            text =
                stringResource(
                    if (isError) R.string.plan_change_error_title else R.string.plan_updated_title
                ),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            fontWeight = FontWeight.SemiBold,
            color = if (isError) TataError else StatusGreen,
        )
        Text(
            text = body,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            color = if (isError) TataError else TataMuted,
        )
    }
}

@Composable
internal fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.plan_title),
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                fontSize = 29.sp,
                color = TataText,
            )
            Text(
                stringResource(R.string.plan_subtitle),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = PlanFont,
                    ),
                color = TataMuted,
            )
        }
    }
}

@Composable
internal fun CurrentPlanCard(subscription: Subscription) {
    TataCard(
        Modifier.fillMaxWidth()
            .tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(listOf(TataMint, Color(0xFFDDEDE1))),
                RoundedCornerShape(18.dp),
            ),
        Color.Transparent,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plan_header, subscription.plan.name),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text =
                    stringResource(
                        R.string.plan_price_per_month,
                        formatPlanPrice(subscription.plan.monthlyPrice, subscription.plan.currency),
                    ),
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.Bold,
                color = TataNavy,
            )
        }
        val locale = LocalConfiguration.current.locales[0]
        val renews = subscription.renewsAt?.let { formatShortDate(it, locale) }
        Text(
            text =
                if (renews != null) {
                    stringResource(R.string.plan_active_renews, renews)
                } else {
                    stringResource(R.string.plan_status_active)
                },
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = stringResource(statusLabel(subscription.status)),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.SemiBold,
                color = StatusGreen,
                modifier =
                    Modifier.padding(top = 10.dp)
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanCard(
    plan: Plan,
    isCurrent: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val container =
        when {
            isCurrent -> TataLavender
            isSelected -> TataBlueSurface
            else -> Color.White
        }
    TataCard(
        modifier =
            Modifier.fillMaxWidth()
                .tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))
                .then(
                    if (isSelected)
                        Modifier.border(BorderStroke(2.dp, TataNavy), RoundedCornerShape(18.dp))
                    else Modifier
                )
                // The current plan cannot be chosen again; the others behave as one choice of a
                // group.
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
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text =
                    if (isCurrent) {
                        stringResource(R.string.plan_current_label)
                    } else {
                        stringResource(
                            R.string.plan_price_per_month,
                            formatPlanPrice(plan.monthlyPrice, plan.currency),
                        )
                    },
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.Bold,
                color = TataNavy,
            )
        }
        val capabilities = featuredPlanCapabilities(plan.capabilities)
        val labels = capabilities.map { stringResource(capabilityLabel(it)) }
        Text(
            text = labels.joinToString(", "),
            fontFamily = PlanFont,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        FlowRow(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            capabilities.forEach { capability ->
                Text(
                    text = "✓ " + stringResource(capabilityLabel(capability)),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontFamily = PlanFont,
                        ),
                    color = TataText,
                )
            }
        }
    }
}

@Composable
internal fun RenewalCard(subscription: Subscription) {
    val locale = LocalConfiguration.current.locales[0]
    TataCard(
        Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
        TataBlueSurface,
    ) {
        Text(
            text = stringResource(R.string.plan_renewal_title),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text =
                    subscription.renewsAt?.let { formatLongDate(it, locale) }
                        ?: stringResource(R.string.plan_renewal_unknown),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontFamily = PlanFont,
                    ),
                fontWeight = FontWeight.Bold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatPlanPrice(subscription.plan.monthlyPrice, subscription.plan.currency),
                fontFamily = PlanFont,
                fontSize = 10.sp,
                color = TataNavy,
                modifier =
                    Modifier.background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
            )
        }
        Text(
            text = stringResource(R.string.plan_renewal_note),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
internal fun ErrorBanner(message: PlanMessage) {
    val body =
        stringResource(
            when (message) {
                PlanMessage.ErrorOffline -> R.string.plan_error_offline
                PlanMessage.ErrorAccount -> R.string.plan_error_account
                PlanMessage.ErrorSession -> R.string.plan_error_session
                PlanMessage.ErrorGeneric -> R.string.plan_error_generic
            }
        )
    TataCard(Modifier.fillMaxWidth(), TataErrorSurface) {
        Text(
            text = stringResource(R.string.plan_error_title),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            fontWeight = FontWeight.SemiBold,
            color = TataError,
        )
        Text(
            text = body,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = PlanFont,
                ),
            color = TataError,
        )
    }
}

internal fun statusLabel(status: SubscriptionStatus): Int =
    when (status) {
        SubscriptionStatus.ACTIVE -> R.string.plan_status_active
    }

internal fun capabilityLabel(capability: PlanCapability): Int =
    when (capability) {
        PlanCapability.REMINDERS -> R.string.plan_capability_reminders
        PlanCapability.AGENDA -> R.string.plan_capability_agenda
        PlanCapability.INTAKE_CONFIRMATION -> R.string.plan_capability_confirmation
        PlanCapability.FAMILY_ALERTS -> R.string.plan_capability_alerts
        PlanCapability.FAMILY_MONITORING -> R.string.plan_capability_monitoring
        PlanCapability.ADHERENCE_INSIGHTS -> R.string.plan_capability_insights
    }

internal fun formatShortDate(instant: Instant, locale: Locale): String =
    DateTimeFormatter.ofPattern("d MMM", locale)
        .withLocale(locale)
        .format(instant.atZone(ZoneId.systemDefault()))

internal fun formatLongDate(instant: Instant, locale: Locale): String =
    DateTimeFormatter.ofPattern("d MMM yyyy", locale)
        .withLocale(locale)
        .format(instant.atZone(ZoneId.systemDefault()))
