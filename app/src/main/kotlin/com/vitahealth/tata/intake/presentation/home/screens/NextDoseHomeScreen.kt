package com.vitahealth.tata.intake.presentation.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.home.mappers.currentDateLabel
import com.vitahealth.tata.intake.presentation.home.ui.DoseDueReminderCard
import com.vitahealth.tata.intake.presentation.home.ui.HomeShortcut
import com.vitahealth.tata.intake.presentation.home.ui.NextDoseCard
import com.vitahealth.tata.intake.presentation.home.ui.NoNextDoseCard
import com.vitahealth.tata.intake.presentation.home.ui.NoNextDoseStatus
import com.vitahealth.tata.intake.presentation.home.ui.ProgressCard
import com.vitahealth.tata.intake.presentation.home.ui.homeInter
import com.vitahealth.tata.intake.presentation.home.ui.homeSerif
import com.vitahealth.tata.intake.presentation.home.ui.isReinforcedReminderDue
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun NextDoseHomeScreen(
    state: NextDoseHomeUiState,
    onRetry: () -> Unit,
    onOpenDoseDetail: (String) -> Unit,
    onOpenAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
    onOpenMedications: (() -> Unit)? = null,
    onOpenNotes: (() -> Unit)? = null,
    onOpenVoice: ((String) -> Unit)? = null,
    currentTime: () -> Instant = Instant::now,
    accountActions: @Composable () -> Unit = {},
) {
    val locale = Locale.forLanguageTag("es-PE")
    val name =
        when (state) {
            is NextDoseHomeUiState.NextDoseAvailable -> state.olderAdultName
            is NextDoseHomeUiState.NoNextDose -> state.olderAdultName
            is NextDoseHomeUiState.Error -> state.olderAdultName
            NextDoseHomeUiState.Loading -> ""
        }
    val progress =
        when (state) {
            is NextDoseHomeUiState.NextDoseAvailable -> state.progress
            is NextDoseHomeUiState.NoNextDose -> state.progress
            else -> null
        }
    var showTip by rememberSaveable { mutableStateOf(true) }
    // A future pending dose becomes due without relying on navigation or another recomposition.
    val pendingDose = (state as? NextDoseHomeUiState.NextDoseAvailable)?.dose
    var reminderClock by
        remember(pendingDose?.id, pendingDose?.scheduledAt) { mutableStateOf(currentTime()) }
    LaunchedEffect(pendingDose?.id, pendingDose?.scheduledAt, pendingDose?.status) {
        if (pendingDose?.status == DoseStatus.PENDING) {
            val remaining = Duration.between(currentTime(), pendingDose.scheduledAt).toMillis()
            if (remaining > 0) delay(remaining + 25)
            reminderClock = currentTime()
        }
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_today),
                        fontFamily = homeSerif,
                        fontSize = 34.sp,
                        color = TataText,
                    )
                    Text(
                        currentDateLabel(locale),
                        fontFamily = homeInter,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = TataMuted,
                    )
                }
                if (name.isNotBlank())
                    Box(
                        Modifier.size(44.dp).background(TataLavender, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            name
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.first().uppercase() },
                            fontFamily = homeInter,
                            color = TataNavy,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
            }
            Spacer(Modifier.height(20.dp))
            when (state) {
                NextDoseHomeUiState.Loading ->
                    TataCard(Modifier.fillMaxWidth(), TataLavender) {
                        Text(stringResource(R.string.home_loading), color = TataMuted)
                    }
                is NextDoseHomeUiState.NextDoseAvailable ->
                    NextDoseCard(state.dose, onOpenDoseDetail)
                is NextDoseHomeUiState.NoNextDose -> NoNextDoseCard()
                is NextDoseHomeUiState.Error ->
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.home_error_title),
                            color = TataText,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            state.message,
                            color = TataMuted,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        TataButton(
                            stringResource(R.string.home_retry),
                            onRetry,
                            Modifier.padding(top = 12.dp),
                        )
                    }
            }
            Spacer(Modifier.height(19.dp))
            ProgressCard(progress, onOpenAgenda)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                val voiceDose = (state as? NextDoseHomeUiState.NextDoseAvailable)?.dose?.id
                HomeShortcut(
                    stringResource(R.string.home_voice_title),
                    stringResource(R.string.home_voice_subtitle),
                    TataMint,
                    R.raw.home_mic,
                    Modifier.weight(1f),
                    if (voiceDose != null && onOpenVoice != null) ({ onOpenVoice(voiceDose) })
                    else null,
                )
                HomeShortcut(
                    stringResource(R.string.home_agenda_title),
                    stringResource(R.string.home_agenda_subtitle),
                    Color(0xFFE1EFF8),
                    R.raw.home_list,
                    Modifier.weight(1f),
                    onOpenAgenda,
                )
            }
            if (showTip) {
                Spacer(Modifier.height(21.dp))
                Row(
                    Modifier.fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFFFF4E2), Color(0xFFFBE9CD))),
                            RoundedCornerShape(18.dp),
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        "☼",
                        fontSize = 20.sp,
                        color = Color(0xFFC48C29),
                        modifier = Modifier.padding(end = 14.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.home_tip_title),
                            fontFamily = homeInter,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC48C29),
                        )
                        Text(
                            stringResource(R.string.home_tip_description),
                            fontFamily = homeInter,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = TataText,
                            modifier = Modifier.padding(top = 7.dp),
                        )
                    }
                    val dismissTip = stringResource(R.string.home_dismiss_tip)
                    IconButton(
                        onClick = { showTip = false },
                        modifier =
                            Modifier.size(24.dp).semantics { contentDescription = dismissTip },
                    ) {
                        Text("×", color = TataMuted)
                    }
                }
            }
            onSignOut?.let { action ->
                TextButton(onClick = action) {
                    Text(stringResource(R.string.home_switch_account), color = TataNavy)
                }
            }
            accountActions()
            Spacer(Modifier.height(24.dp))
        }
        if (
            state is NextDoseHomeUiState.NextDoseAvailable &&
                isReinforcedReminderDue(state.dose, reminderClock)
        ) {
            DoseDueReminderCard(
                medicationName = state.dose.medicationName,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            )
        }
        if (state is NextDoseHomeUiState.NoNextDose) {
            NoNextDoseStatus(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 6.dp)
            )
        }
        AdultTabBar(
            selected = AdultTab.Home,
            availableTabs =
                setOfNotNull(
                    AdultTab.Home,
                    AdultTab.Agenda,
                    AdultTab.Medications.takeIf { onOpenMedications != null },
                    AdultTab.Notes.takeIf { onOpenNotes != null },
                ),
            onSelect = {
                when (it) {
                    AdultTab.Agenda -> onOpenAgenda()
                    AdultTab.Medications -> onOpenMedications?.invoke()
                    AdultTab.Notes -> onOpenNotes?.invoke()
                    else -> Unit
                }
            },
        )
    }
}
