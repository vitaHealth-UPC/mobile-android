package com.vitahealth.tata.intake.presentation.agenda

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Agenda semanal", fontFamily = FontFamily(Font(R.font.agenda_serif)), fontSize = 29.sp, color = TataText)
                    Text(
                        state.week.start.format(DateTimeFormatter.ofPattern("d MMM", agendaLocale)) + " – " +
                            state.week.start.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM yyyy", agendaLocale)),
                        fontSize = 13.sp, color = TataDeepNavy, modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.agenda_calendar), null, Modifier.requiredSize(76.dp).offset(y = 5.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onMoveWeek(-1) }) { Text("Anterior", color = TataDeepNavy) }
                TextButton(onClick = { onMoveWeek(1) }) { Text("Siguiente", color = TataDeepNavy) }
            }
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
                            if (selected) Image(painterResource(R.drawable.agenda_selected), null, Modifier.size(36.dp))
                            Text(day.dayOfMonth.toString(), color = if (selected) Color.White else TataText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
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
            Spacer(Modifier.height(12.dp))
            Row(Modifier.padding(start = 68.dp).fillMaxWidth().background(Color(0xFFEEF2FF), RoundedCornerShape(16.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.agenda_bell), null, Modifier.size(22.dp))
                Text("Un horario fijo facilita la rutina.", fontSize = 11.sp, color = TataText, modifier = Modifier.padding(start = 12.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp)).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.clickable(role = Role.Button, onClick = onHome).padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.agenda_home), null, Modifier.size(22.dp))
                Text("Inicio", fontSize = 10.sp, color = TataDeepNavy)
            }
            Column(Modifier.background(TataLavender, RoundedCornerShape(14.dp)).padding(horizontal = 18.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.agenda_tab), null, Modifier.size(22.dp))
                Text("Agenda", fontSize = 10.sp, color = TataDeepNavy, fontWeight = FontWeight.SemiBold)
            }
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
    val color = when (dose.status) {
        DoseStatus.PENDING -> TataLavender
        DoseStatus.CONFIRMED -> TataMint
        DoseStatus.LATE -> TataCream
        DoseStatus.OMITTED -> TataErrorSurface
    }
    Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(68.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(localTime.format(DateTimeFormatter.ofPattern("h:mm a", agendaLocale)).lowercase(agendaLocale), fontSize = 10.sp, color = TataMuted)
            Image(painterResource(if (localTime.hour < 18) R.drawable.agenda_sun else R.drawable.agenda_moon), null, Modifier.padding(top = 6.dp).size(22.dp))
        }
        TataCard(Modifier.weight(1f).clickable(role = Role.Button) { onOpenDose(dose.id) }, color) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(dose.medicationName, color = TataText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(dose.dose, color = TataText, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    if (dose.instructions.isNotBlank()) Text(dose.instructions, color = TataText, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                    Text(label, color = TataDeepNavy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
                }
                Box(Modifier.padding(start = 8.dp).size(28.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.agenda_status_outer), null, Modifier.size(28.dp))
                    Image(painterResource(R.drawable.agenda_status_inner), null, Modifier.size(23.dp))
                    if (dose.status == DoseStatus.CONFIRMED || dose.status == DoseStatus.LATE) Text("✓", color = TataDeepNavy, fontSize = 14.sp)
                    if (dose.status == DoseStatus.OMITTED) Text("×", color = TataError, fontSize = 14.sp)
                }
            }
        }
    }
}
