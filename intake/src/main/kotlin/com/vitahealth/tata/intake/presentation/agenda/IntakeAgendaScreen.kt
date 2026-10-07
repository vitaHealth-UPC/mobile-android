package com.vitahealth.tata.intake.presentation.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.AdultTab
import com.vitahealth.tata.shared.design.components.AdultTabBar
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val agendaLocale = Locale("es", "PE")

@Composable
fun IntakeAgendaRoute(factory: IntakeAgendaViewModel.Factory, onOpenDose: (String) -> Unit, onHome: () -> Unit) {
    val model: IntakeAgendaViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) model.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    IntakeAgendaScreen(state, model::selectDay, model::moveWeek, model::refresh, onOpenDose, onHome)
}

@Composable
fun IntakeAgendaScreen(
    state: IntakeAgendaUiState,
    onSelectDay: (LocalDate) -> Unit,
    onMoveWeek: (Long) -> Unit,
    onRetry: () -> Unit,
    onOpenDose: (String) -> Unit,
    onHome: () -> Unit,
) {
    ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 11.sp, lineHeight = 14.sp)) {
    Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Agenda semanal", fontFamily = FontFamily(Font(R.font.agenda_serif)), fontSize = 29.sp, lineHeight = 34.sp, color = TataText)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.week.start.format(DateTimeFormatter.ofPattern("d", agendaLocale)) + " – " +
                            state.week.start.plusDays(6).format(DateTimeFormatter.ofPattern("d 'de' MMMM", agendaLocale)),
                            fontSize = 13.sp, color = TataDeepNavy, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onMoveWeek(-1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Anterior" }) { Text("‹", color = TataDeepNavy) }
                        IconButton(onClick = { onMoveWeek(1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Siguiente" }) { Text("›", color = TataDeepNavy) }
                    }
                }
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    TataSvgIcon(R.raw.agenda_calendar, Modifier.requiredSize(76.dp).offset(y = 5.dp))
                    TataSvgIcon(R.raw.agenda_calendar_header, Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                state.week.days.forEach { day ->
                    val selected = day == state.selectedDay
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(listOf("L", "M", "M", "J", "V", "S", "D")[day.dayOfWeek.value - 1], fontSize = 10.sp, color = TataMuted)
                        Box(
                            Modifier.padding(top = 8.dp).size(44.dp)
                                .clickable(role = Role.Button) { onSelectDay(day) }
                                .semantics { contentDescription = day.format(DateTimeFormatter.ofPattern("EEEE d MMMM", agendaLocale)) + if (selected) ", seleccionado" else "" },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) TataSvgIcon(R.raw.agenda_selected, Modifier.size(36.dp))
                            Text(day.dayOfMonth.toString(), color = if (selected) Color.White else TataText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            when {
                state.loading -> TataCard(Modifier.fillMaxWidth(), TataLavender) { Text("Consultando tu agenda…", color = TataText) }
                state.error != null -> TataCard(Modifier.fillMaxWidth()) {
                    Text(state.error, color = TataText)
                    TataButton("Reintentar", onRetry, Modifier.padding(top = 12.dp))
                }
                state.selectedDoses.isEmpty() -> TataCard(Modifier.fillMaxWidth(), TataLavender) {
                    Text("No hay tomas programadas para este día.", color = TataText)
                }
                else -> state.selectedDoses.forEach { dose -> AgendaDoseRow(dose, state, onOpenDose) }
            }
            Row(Modifier.padding(start = 68.dp).fillMaxWidth().shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
                .background(Brush.horizontalGradient(listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))), RoundedCornerShape(14.dp))
                .heightIn(min = 64.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TataSvgIcon(R.raw.agenda_bell, Modifier.size(22.dp))
                Column(Modifier.padding(start = 8.dp)) {
                    Text(stringResource(R.string.agenda_tip_title), fontSize = 11.sp, color = TataDeepNavy)
                    Spacer(Modifier.height(5.dp))
                    Text(stringResource(R.string.agenda_tip_body), fontSize = 11.sp, lineHeight = 14.sp, color = TataText)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        AdultTabBar(AdultTab.Agenda, { if (it == AdultTab.Home) onHome() }, setOf(AdultTab.Home, AdultTab.Agenda))
    }
    }
}

@Composable
private fun AgendaDoseRow(dose: DoseDetailReadModel, state: IntakeAgendaUiState, onOpenDose: (String) -> Unit) {
    val localTime = dose.scheduledAt.atZone(state.week.zone)
    val label = when (dose.status) {
        DoseStatus.PENDING -> "Pendiente"
        DoseStatus.CONFIRMED -> "Confirmada"
        DoseStatus.LATE -> "Tardía"
        DoseStatus.OMITTED -> "Omitida"
    }
    val colors = when {
        dose.status == DoseStatus.OMITTED -> listOf(TataErrorSurface, Color(0xFFFADFE1))
        dose.status == DoseStatus.LATE || localTime.hour in 12..17 -> listOf(Color(0xFFFFF5E3), Color(0xFFFBE9C7))
        localTime.hour >= 21 -> listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))
        else -> listOf(Color(0xFFF1ECFF), Color(0xFFE7DFFC))
    }
    Row(Modifier.fillMaxWidth().drawBehind {
        drawLine(Color(0xFFE6E3F1), Offset(49.dp.toPx(), 0f), Offset(49.dp.toPx(), size.height), 2.dp.toPx())
    }.padding(bottom = if (dose.id == state.selectedDoses.lastOrNull()?.id) 12.dp else 28.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(68.dp), horizontalAlignment = Alignment.End) {
            Text(localTime.format(DateTimeFormatter.ofPattern("h:mm a", agendaLocale)).lowercase(agendaLocale), fontSize = 10.sp, color = TataMuted)
            TataSvgIcon(if (localTime.hour < 18) R.raw.agenda_sun else R.raw.agenda_moon, Modifier.padding(top = 6.dp, end = 8.dp).size(22.dp).background(TataSurface))
        }
        Row(Modifier.weight(1f).shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
            .background(Brush.horizontalGradient(colors), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onOpenDose(dose.id) }
            .semantics { contentDescription = "${dose.medicationName}, $label" }
            .heightIn(min = 88.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(dose.medicationName, color = TataText, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(dose.dose, color = TataText, fontSize = 11.sp, lineHeight = 14.sp)
                    if (dose.instructions.isNotBlank()) {
                        Spacer(Modifier.height(7.dp))
                        Text(dose.instructions, color = TataText, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
                Box(Modifier.padding(start = 8.dp).size(28.dp), contentAlignment = Alignment.Center) {
                    TataSvgIcon(R.raw.agenda_status_outer, Modifier.size(28.dp))
                    TataSvgIcon(R.raw.agenda_status_inner, Modifier.size(23.dp))
                    if (dose.status == DoseStatus.CONFIRMED || dose.status == DoseStatus.LATE) Text("✓", color = TataSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (dose.status == DoseStatus.OMITTED) Text("×", color = TataError, fontSize = 14.sp)
                }
        }
    }
}
