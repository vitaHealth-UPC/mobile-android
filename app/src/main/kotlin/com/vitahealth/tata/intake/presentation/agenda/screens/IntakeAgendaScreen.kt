package com.vitahealth.tata.intake.presentation.agenda.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.agenda.ui.AgendaDoseRow
import com.vitahealth.tata.intake.presentation.agenda.ui.agendaLocale
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaUiState
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IntakeAgendaScreen(
    state: IntakeAgendaUiState,
    onSelectDay: (LocalDate) -> Unit,
    onMoveWeek: (Long) -> Unit,
    onRetry: () -> Unit,
    onOpenDose: (String) -> Unit,
    onHome: () -> Unit,
    onMedications: (() -> Unit)? = null,
    onNotes: (() -> Unit)? = null,
) {
    ProvideTextStyle(
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    ) {
        Column(
            Modifier.fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF)))
                )
        ) {
            Column(
                Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(28.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Agenda semanal",
                            fontFamily = FontFamily(Font(R.font.agenda_serif)),
                            fontSize = 29.sp,
                            lineHeight = 34.sp,
                            color = TataText,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.week.start.format(
                                    DateTimeFormatter.ofPattern("d", agendaLocale)
                                ) +
                                    " – " +
                                    state.week.start
                                        .plusDays(6)
                                        .format(
                                            DateTimeFormatter.ofPattern("d 'de' MMMM", agendaLocale)
                                        ),
                                fontSize = 13.sp,
                                color = TataDeepNavy,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { onMoveWeek(-1) },
                                modifier =
                                    Modifier.size(32.dp).semantics {
                                        contentDescription = "Anterior"
                                    },
                            ) {
                                Text("‹", color = TataDeepNavy)
                            }
                            IconButton(
                                onClick = { onMoveWeek(1) },
                                modifier =
                                    Modifier.size(32.dp).semantics {
                                        contentDescription = "Siguiente"
                                    },
                            ) {
                                Text("›", color = TataDeepNavy)
                            }
                        }
                    }
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        TataSvgIcon(
                            R.raw.agenda_calendar,
                            Modifier.requiredSize(76.dp).offset(y = 5.dp),
                        )
                        TataSvgIcon(R.raw.agenda_calendar_header, Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    state.week.days.forEach { day ->
                        val selected = day == state.selectedDay
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                listOf("L", "M", "M", "J", "V", "S", "D")[day.dayOfWeek.value - 1],
                                fontSize = 10.sp,
                                color = TataMuted,
                            )
                            Box(
                                Modifier.padding(top = 8.dp)
                                    .size(44.dp)
                                    .clickable(role = Role.Button) { onSelectDay(day) }
                                    .semantics {
                                        contentDescription =
                                            day.format(
                                                DateTimeFormatter.ofPattern(
                                                    "EEEE d MMMM",
                                                    agendaLocale,
                                                )
                                            ) + if (selected) ", seleccionado" else ""
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected)
                                    TataSvgIcon(R.raw.agenda_selected, Modifier.size(36.dp))
                                Text(
                                    day.dayOfMonth.toString(),
                                    color = if (selected) Color.White else TataText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                when {
                    state.loading ->
                        TataCard(Modifier.fillMaxWidth(), TataLavender) {
                            Text("Consultando tu agenda…", color = TataText)
                        }
                    state.error != null ->
                        TataCard(Modifier.fillMaxWidth()) {
                            Text(state.error, color = TataText)
                            TataButton("Reintentar", onRetry, Modifier.padding(top = 12.dp))
                        }
                    state.selectedDoses.isEmpty() ->
                        TataCard(Modifier.fillMaxWidth(), TataLavender) {
                            Text("No hay tomas programadas para este día.", color = TataText)
                        }
                    else ->
                        state.selectedDoses.forEach { dose ->
                            AgendaDoseRow(dose, state, onOpenDose)
                        }
                }
                Row(
                    Modifier.padding(start = 68.dp)
                        .fillMaxWidth()
                        .shadow(
                            6.dp,
                            RoundedCornerShape(14.dp),
                            ambientColor = Color(0x141A2138),
                            spotColor = Color(0x141A2138),
                        )
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))),
                            RoundedCornerShape(14.dp),
                        )
                        .heightIn(min = 64.dp)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TataSvgIcon(R.raw.agenda_bell, Modifier.size(22.dp))
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(
                            stringResource(R.string.agenda_tip_title),
                            fontSize = 11.sp,
                            color = TataDeepNavy,
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            stringResource(R.string.agenda_tip_body),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = TataText,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            AdultTabBar(
                AdultTab.Agenda,
                {
                    when (it) {
                        AdultTab.Home -> onHome()
                        AdultTab.Medications -> onMedications?.invoke()
                        AdultTab.Notes -> onNotes?.invoke()
                        else -> Unit
                    }
                },
                setOfNotNull(
                    AdultTab.Home,
                    AdultTab.Agenda,
                    AdultTab.Medications.takeIf { onMedications != null },
                    AdultTab.Notes.takeIf { onNotes != null },
                ),
            )
        }
    }
}
