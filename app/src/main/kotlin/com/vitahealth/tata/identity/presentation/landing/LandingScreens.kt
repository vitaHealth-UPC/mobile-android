package com.vitahealth.tata.identity.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import java.math.RoundingMode

private val navy = Color(0xFF173B70)
private val ink = Color(0xFF0E1729)
private val muted = Color(0xFF7B879B)
private val surface = Color(0xFFF8F8FE)
private val inter = FontFamily(Font(R.font.tata_inter))
private val serif = FontFamily(Font(R.font.tata_serif))

/** Figma #1: introductory page; the miniature schedule is an illustration, not patient data. */
@Composable
fun LandingValueScreen(onNext: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(surface).verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 25.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tata", fontFamily = serif, color = navy, fontSize = 41.sp)
            Spacer(Modifier.width(8.dp))
            TataSvgIcon(R.raw.onboarding_butterfly, Modifier.size(28.dp))
        }
        Text(stringResource(R.string.landing_tagline), fontFamily = inter, fontSize = 12.sp, color = muted)
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF0ECFC)) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 224.dp).padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Text(
                        stringResource(R.string.landing_hero_title),
                        fontFamily = serif, fontSize = 21.sp, lineHeight = 26.sp, color = ink,
                    )
                    Text(
                        stringResource(R.string.landing_hero_subtitle),
                        fontFamily = inter, fontSize = 11.sp, color = muted,
                    )
                    Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = navy)) {
                        Text(stringResource(R.string.landing_continue))
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 142.dp),
                    shape = RoundedCornerShape(17.dp), color = Color.White,
                ) {
                    Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Text(stringResource(R.string.landing_example), fontSize = 9.sp, color = muted)
                        Text(stringResource(R.string.landing_preview_schedule), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ink)
                        Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE5E8F3), RoundedCornerShape(4.dp))) {
                            Box(Modifier.fillMaxWidth(0.65f).height(6.dp).background(Color(0xFF6BAB83), RoundedCornerShape(4.dp)))
                        }
                        Text(stringResource(R.string.landing_preview_info), fontSize = 10.sp, color = navy)
                    }
                }
            }
        }
        Text(stringResource(R.string.landing_features), fontSize = 17.sp, color = ink, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val features = listOf(
                Triple(R.string.landing_reminders, R.string.landing_reminders_description, Color(0xFFE8F5EB)),
                Triple(R.string.landing_family, R.string.landing_family_description, Color(0xFFFFF3E2)),
                Triple(R.string.landing_insights, R.string.landing_insights_description, Color(0xFFF1ECFF)),
            )
            features.forEach { (title, description, bg) ->
                Surface(Modifier.weight(1f).heightIn(min = 111.dp), shape = RoundedCornerShape(17.dp), color = bg) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(stringResource(title), fontSize = 11.sp, fontFamily = inter, fontWeight = FontWeight.SemiBold, color = ink)
                        Text(stringResource(description), fontSize = 10.sp, fontFamily = inter, lineHeight = 13.sp, color = muted)
                    }
                }
            }
        }
        Text(stringResource(R.string.landing_for_whom), fontSize = 13.sp, color = navy)
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFFFF3E3)) {
            Text(stringResource(R.string.landing_trust_description),
                Modifier.fillMaxWidth().padding(20.dp), fontFamily = inter, fontSize = 12.sp, color = ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LandingFact(stringResource(R.string.landing_three_steps), stringResource(R.string.landing_three_steps_description), Modifier.weight(1f))
            LandingFact(stringResource(R.string.landing_two_roles), stringResource(R.string.landing_two_roles_description), Modifier.weight(1f))
        }
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = navy)) {
            Text(stringResource(R.string.landing_view_plans))
        }
    }
}

@Composable
private fun LandingFact(number: String, description: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.padding(14.dp)) {
            Text(number, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = navy)
            Text(description, fontSize = 10.sp, color = muted)
        }
    }
}

@Composable
fun LandingPlansRoute(
    factory: LandingPlansViewModel.Factory,
    onBack: () -> Unit,
    onSelectPlan: (Plan) -> Unit,
    onContact: (() -> Unit)? = null,
) {
    val vm: LandingPlansViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LandingPlansScreen(state, onBack, vm::refresh, onSelectPlan, onContact)
}

/** Figma #2/#69. No fabricated plan amounts or external contact destination. */
@Composable
fun LandingPlansScreen(
    state: LandingPlansUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectPlan: (Plan) -> Unit,
    onContact: (() -> Unit)? = null,
) {
    Column(
        Modifier.fillMaxSize().background(surface).verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.landing_back)) }
        Text(stringResource(R.string.landing_choose_title),
            fontFamily = serif, fontSize = 29.sp, color = ink)
        Text(stringResource(R.string.landing_choose_subtitle),
            fontFamily = inter, fontSize = 12.sp, color = muted)
        when {
            state.loading -> CircularProgressIndicator()
            state.error -> {
                Text(stringResource(R.string.landing_plan_error), color = muted)
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.landing_retry)) }
            }
            else -> state.plans.forEach { plan ->
                val family = plan.supports(PlanCapability.FAMILY_MONITORING) ||
                    plan.supports(PlanCapability.ADHERENCE_INSIGHTS)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (family) Color(0xFFF0EAFD) else Color.White,
                ) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = if (family) 142.dp else 118.dp)
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(plan.name, Modifier.weight(1f),
                                fontSize = 17.sp, fontFamily = inter,
                                fontWeight = FontWeight.SemiBold, color = ink)
                            Text(planPrice(plan), fontSize = 15.sp, color = navy,
                                fontWeight = FontWeight.Bold)
                        }
                        Text(
                            stringResource(if (family) R.string.landing_family_description
                                else R.string.landing_essential_description),
                            fontSize = 12.sp, color = muted, fontFamily = inter,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onSelectPlan(plan) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (family) navy else Color(0xFFDAD0F4),
                                    contentColor = if (family) Color.White else navy,
                                ),
                            ) { Text(stringResource(R.string.landing_choose_plan, plan.name)) }
                            if (family) {
                                Spacer(Modifier.width(12.dp))
                                Text(stringResource(R.string.landing_recommended),
                                    color = navy, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        if (!state.loading && !state.error) {
            Text(stringResource(R.string.landing_choice_notice), fontSize = 11.sp, color = muted)
        }
        Surface(
            shape = RoundedCornerShape(18.dp), color = Color(0xFFEAF2FB),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.landing_questions), Modifier.weight(1f),
                        fontSize = 17.sp, color = ink, fontWeight = FontWeight.SemiBold)
                    TataSvgIcon(R.raw.tata_linked_call, Modifier.size(22.dp))
                    Spacer(Modifier.width(7.dp))
                    TataSvgIcon(R.raw.tata_linked_message, Modifier.size(22.dp))
                }
                Text(stringResource(R.string.landing_contact_description), fontSize = 12.sp, color = muted)
                Button(
                    onClick = { onContact?.invoke() },
                    enabled = onContact != null,
                    colors = ButtonDefaults.buttonColors(containerColor = navy),
                ) {
                    Text(stringResource(if (onContact == null)
                        R.string.landing_contact_unavailable else R.string.landing_contact))
                }
            }
        }
        if (!state.loading && !state.error) {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White) {
                Column(Modifier.fillMaxWidth().padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.landing_plan_comparison), Modifier.weight(2f),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ink)
                        state.plans.forEach { plan ->
                            Text(plan.name, Modifier.weight(1f), fontSize = 10.sp,
                                color = muted)
                        }
                    }
                    val features = listOf(
                        R.string.landing_reminders to PlanCapability.REMINDERS,
                        R.string.landing_tracking to PlanCapability.FAMILY_MONITORING,
                        R.string.landing_adherence_insights to PlanCapability.ADHERENCE_INSIGHTS,
                    )
                    features.forEach { (label, capability) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(label), Modifier.weight(2f),
                                fontSize = 11.sp, color = muted)
                            state.plans.forEach { plan ->
                                Text(if (plan.supports(capability)) "✓" else "–",
                                    Modifier.weight(1f), fontSize = 12.sp,
                                    color = if (plan.supports(capability))
                                        Color(0xFF2E734D) else muted)
                            }
                        }
                    }
                }
            }
            Text(stringResource(R.string.landing_footer), fontSize = 10.sp, color = muted)
        }
    }
}

private fun planPrice(plan: Plan): String {
    val cost = plan.monthlyPrice.setScale(2, RoundingMode.HALF_UP).toPlainString()
    return (if (plan.currency == "PEN") "S/ " else plan.currency + " ") + cost
}
