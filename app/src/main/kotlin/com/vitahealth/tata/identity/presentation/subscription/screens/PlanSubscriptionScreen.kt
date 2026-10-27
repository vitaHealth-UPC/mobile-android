package com.vitahealth.tata.identity.presentation.subscription.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import com.vitahealth.tata.identity.presentation.subscription.ui.ChangeBanner
import com.vitahealth.tata.identity.presentation.subscription.ui.ChangeDialog
import com.vitahealth.tata.identity.presentation.subscription.ui.CurrentPlanCard
import com.vitahealth.tata.identity.presentation.subscription.ui.ErrorBanner
import com.vitahealth.tata.identity.presentation.subscription.ui.Header
import com.vitahealth.tata.identity.presentation.subscription.ui.PlanCard
import com.vitahealth.tata.identity.presentation.subscription.ui.PlanFont
import com.vitahealth.tata.identity.presentation.subscription.ui.RenewalCard
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.math.BigDecimal
import java.time.Instant

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
    onTabSelected: (CaregiverTab) -> Unit = { onBack() },
    onSignIn: () -> Unit = onBack,
) {
    Column(modifier.fillMaxSize().background(TataSurface)) {
        Column(
            modifier =
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Header()
            Spacer(Modifier.height(16.dp))

            val subscription = state.subscription
            when {
                state.isLoading ->
                    TataCard(Modifier.fillMaxWidth(), TataMint) {
                        Text(stringResource(R.string.plan_loading), color = TataText)
                    }

                subscription == null -> {
                    state.message?.let { ErrorBanner(it) }
                    Spacer(Modifier.height(12.dp))
                    val recoverAccount =
                        state.message == PlanMessage.ErrorSession ||
                            state.message == PlanMessage.ErrorAccount
                    TataButton(
                        text =
                            stringResource(
                                if (recoverAccount) R.string.plan_sign_in else R.string.plan_retry
                            ),
                        onClick = if (recoverAccount) onSignIn else onRetry,
                    )
                }

                else -> {
                    CurrentPlanCard(subscription)
                    state.message?.let {
                        Spacer(Modifier.height(12.dp))
                        ErrorBanner(it)
                        TataButton(
                            stringResource(R.string.plan_retry),
                            onRetry,
                            Modifier.padding(top = 8.dp),
                        )
                    }
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
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontFamily = PlanFont,
                                ),
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
        CaregiverTabBar(CaregiverTab.More, onTabSelected)
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanSubscriptionScreenPreview() {
    val essential =
        Plan(
            "ESSENTIAL",
            "Esencial",
            BigDecimal("9.90"),
            "PEN",
            setOf(
                PlanCapability.REMINDERS,
                PlanCapability.AGENDA,
                PlanCapability.INTAKE_CONFIRMATION,
            ),
        )
    val family =
        Plan("FAMILY", "Familiar", BigDecimal("19.90"), "PEN", PlanCapability.entries.toSet())
    TataTheme {
        PlanSubscriptionScreen(
            state =
                PlanSubscriptionUiState(
                    isLoading = false,
                    subscription =
                        Subscription(
                            "acc",
                            family,
                            SubscriptionStatus.ACTIVE,
                            Instant.parse("2026-10-28T00:00:00Z"),
                        ),
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
