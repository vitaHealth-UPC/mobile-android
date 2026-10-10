package com.vitahealth.tata.intake.presentation.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailInter = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter))
private val detailSerif = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif))

@Composable
fun DoseDetailRoute(
    factory: DoseDetailViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: () -> Unit = onBack,
    onAdultTab: ((AdultTab, String) -> Unit)? = null,
) {
    val viewModel: DoseDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    DoseDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onConfirm = viewModel::confirm,
        modifier = modifier,
        onHome = onHome,
        onAdultTab = onAdultTab,
    )
}

@Composable
fun DoseDetailScreen(
    state: DoseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: () -> Unit = onBack,
    onAdultTab: ((AdultTab, String) -> Unit)? = null,
) {
    if (
        state is DoseDetailUiState.Content &&
            (state.confirmationSucceeded || state.outcome == ConfirmationOutcome.OMISSION_PRESERVED)
    ) {
        DoseOutcomeScreen(
            state,
            onHome,
            modifier,
            onTab =
                onAdultTab?.let { callback -> { tab -> callback(tab, state.dose.olderAdultId) } },
        )
        return
    }
    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF)))
            )
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            val backLabel = stringResource(R.string.detail_back)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp).semantics { contentDescription = backLabel },
                ) {
                    Text("‹", color = TataNavy, fontSize = 24.sp)
                }
                Text(
                    stringResource(R.string.detail_title),
                    fontFamily = detailSerif,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = TataText,
                )
            }
            Spacer(Modifier.height(22.dp))
            when (state) {
                DoseDetailUiState.Loading ->
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.detail_loading), color = TataMuted)
                    }
                is DoseDetailUiState.Error ->
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.detail_error_title),
                            fontWeight = FontWeight.Bold,
                            color = TataText,
                        )
                        Text(
                            state.message,
                            color = TataMuted,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        TataButton(
                            stringResource(R.string.home_retry),
                            onRetry,
                            Modifier.padding(top = 16.dp),
                        )
                    }
                is DoseDetailUiState.Content -> {
                    DoseContent(state.dose, state.nextDose)
                    state.confirmationMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    if (state.confirmationUnavailable) {
                        TataButton(stringResource(R.string.home_retry), onRetry)
                    } else if (state.dose.status == DoseStatus.PENDING) {
                        TataButton(
                            stringResource(
                                if (state.confirming) R.string.detail_confirming
                                else R.string.detail_confirm
                            ),
                            onConfirm,
                            enabled = !state.confirming,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        val owner = (state as? DoseDetailUiState.Content)?.dose?.olderAdultId
        AdultTabBar(
            selected = AdultTab.Medications,
            availableTabs =
                if (owner != null && onAdultTab != null)
                    setOf(AdultTab.Home, AdultTab.Medications, AdultTab.Agenda, AdultTab.Notes)
                else emptySet(),
            onSelect = { tab -> if (owner != null) onAdultTab?.invoke(tab, owner) },
        )
    }
}

@Composable
private fun DoseContent(
    dose: DoseDetailReadModel,
    nextDose: com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel?,
) {
    val scheduledDose =
        nextDose?.let {
            DoseDetailReadModel(
                it.id,
                it.treatmentId,
                it.medicationId,
                it.olderAdultId,
                it.medicationName,
                it.dose,
                it.instructions,
                it.scheduledAt,
                it.status,
            )
        } ?: dose
    Row(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFF0EBFF), Color(0xFFE7DDFB))))
            .padding(start = 3.dp, end = 16.dp, top = 9.dp, bottom = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(90.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.detail_tablet),
                null,
                Modifier.requiredSize(130.dp).offset(y = 10.dp),
            )
        }
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(
                dose.medicationName,
                fontFamily = detailInter,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
            )
            Text(
                statusTitle(dose.status),
                fontFamily = detailInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = Color(0xFF5966B8),
                modifier =
                    Modifier.padding(top = 5.dp)
                        .background(Color(0xFFF8F6FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp),
            )
            Text(
                dose.dose,
                fontFamily = detailInter,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = TataText,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    DetailSectionLabel(
        stringResource(
            if (nextDose != null || dose.status == DoseStatus.PENDING)
                R.string.detail_next_confirmation
            else R.string.detail_recorded_schedule
        )
    )
    Row(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TataSvgIcon(R.raw.detail_morning, Modifier.size(22.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                scheduleDayLabel(scheduledDose),
                fontFamily = detailInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = TataMuted,
            )
            Text(
                scheduleTimeLabel(scheduledDose),
                fontFamily = detailInter,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
            )
        }
        Text(
            statusTitle(scheduledDose.status),
            fontFamily = detailInter,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = if (scheduledDose.status == DoseStatus.PENDING) Color(0xFFC48C29) else TataNavy,
            modifier =
                Modifier.background(statusColor(scheduledDose.status), RoundedCornerShape(13.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
    DetailSectionLabel(stringResource(R.string.detail_information))
    Column(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        DetailLine(R.raw.detail_dose, stringResource(R.string.detail_dose), dose.dose)
        DetailLine(R.raw.detail_when, stringResource(R.string.detail_when), scheduleTimeLabel(dose))
        // The intake contract does not provide clinical indication or treatment frequency.
        DetailLine(
            R.raw.detail_purpose,
            stringResource(R.string.detail_purpose),
            stringResource(R.string.detail_unspecified),
        )
        DetailLine(
            R.raw.detail_instructions,
            stringResource(R.string.detail_instructions),
            dose.instructions.ifBlank { stringResource(R.string.detail_no_instructions) },
        )
    }
    Spacer(Modifier.height(19.dp))
    Box(
        Modifier.fillMaxWidth()
            .heightIn(min = 128.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFF1ECFF), Color(0xFFE5DCFA))),
                RoundedCornerShape(18.dp),
            )
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)) {
            Text(
                stringResource(R.string.detail_before_dose),
                fontFamily = detailInter,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataNavy,
            )
            Column(
                Modifier.padding(top = 12.dp, end = 72.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreparationLine("✓", stringResource(R.string.detail_prepare_medication))
                PreparationLine("✓", stringResource(R.string.detail_prepare_water))
                PreparationLine("○", stringResource(R.string.detail_prepare_seat))
            }
        }
        Image(
            painterResource(R.drawable.detail_water_plant),
            null,
            Modifier.align(Alignment.BottomEnd).padding(end = 9.dp).size(106.dp, 102.dp),
        )
    }
    Column(
        Modifier.fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                when (dose.status) {
                    DoseStatus.OMITTED -> Color(0xFFFFF5E0)
                    DoseStatus.CONFIRMED -> Color(0xFFE8F5EB)
                    else -> TataLavender
                },
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            stringResource(R.string.detail_state, statusTitle(dose.status)),
            color =
                when (dose.status) {
                    DoseStatus.OMITTED -> Color(0xFF855E1F)
                    DoseStatus.CONFIRMED -> Color(0xFF2E6E45)
                    else -> TataNavy
                },
            fontFamily = detailInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            statusMessage(dose.status),
            color = TataMuted,
            fontFamily = detailInter,
            fontSize = 9.sp,
            lineHeight = 12.sp,
        )
    }
    Spacer(Modifier.height(if (dose.status == DoseStatus.PENDING) 20.dp else 4.dp))
}

@Composable
private fun DetailSectionLabel(label: String) {
    Text(
        label,
        fontFamily = detailInter,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = TataMuted,
        modifier = Modifier.padding(top = 23.dp, bottom = 8.dp),
    )
}

@Composable
private fun DetailLine(icon: Int, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TataSvgIcon(icon, Modifier.size(20.dp))
        Text(
            label,
            fontFamily = detailInter,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = TataMuted,
            modifier = Modifier.padding(start = 10.dp).width(102.dp),
        )
        Text(
            value,
            fontFamily = detailInter,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TataText,
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun PreparationLine(mark: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(mark, fontSize = 14.sp, color = Color(0xFF6E6ED1), modifier = Modifier.width(26.dp))
        Text(text, fontFamily = detailInter, fontSize = 13.sp, lineHeight = 17.sp, color = TataText)
    }
}

@Composable
private fun scheduleDayLabel(dose: DoseDetailReadModel): String {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    return dose.scheduledAt
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE", locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

@Composable
private fun scheduleTimeLabel(dose: DoseDetailReadModel): String {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    return dose.scheduledAt
        .atZone(ZoneId.systemDefault())
        .format(
            DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT).withLocale(locale)
        )
}

@Composable
private fun statusTitle(status: DoseStatus): String =
    stringResource(
        when (status) {
            DoseStatus.PENDING -> R.string.detail_status_pending
            DoseStatus.CONFIRMED -> R.string.detail_status_confirmed
            DoseStatus.LATE -> R.string.detail_status_late
            DoseStatus.OMITTED -> R.string.detail_status_omitted
        }
    )

@Composable
private fun statusMessage(status: DoseStatus): String =
    stringResource(
        when (status) {
            DoseStatus.PENDING -> R.string.detail_message_pending
            DoseStatus.CONFIRMED -> R.string.detail_message_confirmed
            DoseStatus.LATE -> R.string.detail_message_late
            DoseStatus.OMITTED -> R.string.detail_message_omitted
        }
    )

private fun statusColor(status: DoseStatus): Color =
    when (status) {
        DoseStatus.PENDING -> TataCream
        DoseStatus.CONFIRMED -> TataMint
        DoseStatus.LATE -> TataCream
        DoseStatus.OMITTED -> TataWarningSurface
    }
