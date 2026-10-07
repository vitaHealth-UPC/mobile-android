package com.vitahealth.tata.intake.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.R
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.*
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val homeInter = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter))
private val homeSerif = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif))

@Composable
fun NextDoseHomeRoute(
    factory: NextDoseHomeViewModel.Factory,
    onOpenDoseDetail: (String) -> Unit,
    onOpenAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
) {
    val viewModel: NextDoseHomeViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.retry() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    NextDoseHomeScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpenDoseDetail = onOpenDoseDetail,
        onOpenAgenda = onOpenAgenda,
        modifier = modifier,
        onSignOut = onSignOut,
    )
}

@Composable
fun NextDoseHomeScreen(
    state: NextDoseHomeUiState,
    onRetry: () -> Unit,
    onOpenDoseDetail: (String) -> Unit,
    onOpenAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
) {
    val name = when (state) {
        is NextDoseHomeUiState.NextDoseAvailable -> state.olderAdultName
        is NextDoseHomeUiState.NoNextDose -> state.olderAdultName
        is NextDoseHomeUiState.Error -> state.olderAdultName
        NextDoseHomeUiState.Loading -> ""
    }
    val progress = when (state) {
        is NextDoseHomeUiState.NextDoseAvailable -> state.progress
        is NextDoseHomeUiState.NoNextDose -> state.progress
        else -> null
    }
    var showTip by remember { mutableStateOf(true) }
    Column(modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hoy", fontFamily = homeSerif, fontSize = 34.sp, color = TataText)
                    Text(currentDateLabel(), fontFamily = homeInter, fontSize = 13.sp, color = TataMuted)
                }
                if (name.isNotBlank()) Box(Modifier.size(44.dp).background(TataLavender, CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
                        fontFamily = homeInter, color = TataNavy, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            when (state) {
                NextDoseHomeUiState.Loading -> TataCard(Modifier.fillMaxWidth(), TataLavender) { Text("Consultando tu programación…", color = TataMuted) }
                is NextDoseHomeUiState.NextDoseAvailable -> NextDoseCard(state.dose, onOpenDoseDetail)
                is NextDoseHomeUiState.NoNextDose -> TataCard(Modifier.fillMaxWidth(), TataLavender) {
                    Text("Sin próxima toma", color = TataNavy, fontWeight = FontWeight.SemiBold)
                    Text("No hay tomas pendientes programadas.", color = TataText, modifier = Modifier.padding(top = 12.dp))
                }
                is NextDoseHomeUiState.Error -> TataCard(Modifier.fillMaxWidth()) {
                    Text("No pudimos cargar tu próxima toma", color = TataText, fontWeight = FontWeight.Bold)
                    Text(state.message, color = TataMuted, modifier = Modifier.padding(top = 8.dp))
                    TataButton("Reintentar", onRetry, Modifier.padding(top = 12.dp))
                }
            }
            Spacer(Modifier.height(19.dp))
            ProgressCard(progress, onOpenAgenda)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                // Voice recognition has no route yet; do not pretend that a tap confirms an intake.
                HomeShortcut("Confirmar\ncon voz", "Es rápido y fácil", TataMint, Modifier.weight(1f), null)
                HomeShortcut("Ver todas\nmis tomas", "Horario completo", Color(0xFFE1EFF8), Modifier.weight(1f), onOpenAgenda)
            }
            if (showTip) {
                Spacer(Modifier.height(21.dp))
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFFFF4E2), Color(0xFFFBE9CD))), RoundedCornerShape(18.dp)).padding(16.dp)) {
                    Text("☼", fontSize = 20.sp, color = Color(0xFFC48C29), modifier = Modifier.padding(end = 14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Consejo del día", fontFamily = homeInter, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC48C29))
                        Text("Toma tus medicamentos con agua y a la misma hora cada día.", fontFamily = homeInter, fontSize = 14.sp, lineHeight = 18.sp, color = TataText, modifier = Modifier.padding(top = 7.dp))
                    }
                    IconButton(onClick = { showTip = false }, modifier = Modifier.size(24.dp)) { Text("×", color = TataMuted) }
                }
            }
            onSignOut?.let { action -> TextButton(onClick = action) { Text("Cambiar cuenta", color = TataNavy) } }
            Spacer(Modifier.height(24.dp))
        }
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth().shadow(6.dp, RoundedCornerShape(28.dp)).background(Color.White, RoundedCornerShape(28.dp)).padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Inicio", fontFamily = homeInter, color = TataNavy, fontWeight = FontWeight.SemiBold)
            Text("Agenda", fontFamily = homeInter, color = TataMuted, modifier = Modifier.clickable(role = Role.Button, onClick = onOpenAgenda))
        }
    }
}

@Composable
private fun NextDoseCard(dose: NextDoseReadModel, onOpenDoseDetail: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
        .background(Brush.horizontalGradient(listOf(Color(0xFFEEE8FF), Color(0xFFE5DDF8))))
        .clickable(role = Role.Button) { onOpenDoseDetail(dose.id) }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Próxima toma", fontFamily = homeInter, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TataNavy)
            Text(dose.scheduledAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a", Locale("es", "PE"))).lowercase().replace("am", "a. m.").replace("pm", "p. m."),
                fontFamily = homeInter, fontSize = 31.sp, fontWeight = FontWeight.Bold, color = TataText, modifier = Modifier.padding(top = 12.dp))
            Text(dose.medicationName, fontFamily = homeInter, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TataText, modifier = Modifier.padding(top = 7.dp))
            Text(dose.dose, fontFamily = homeInter, fontSize = 13.sp, color = TataMuted, modifier = Modifier.padding(top = 4.dp))
            relativeTimeLabel(dose)?.let { label ->
                Text(label, fontFamily = homeInter, fontSize = 12.sp, color = Color(0xFF5961B2), modifier = Modifier.padding(top = 8.dp).background(Color.White, CircleShape).padding(horizontal = 18.dp, vertical = 4.dp))
            }
        }
        Image(painterResource(R.drawable.home_tablet), null, Modifier.size(132.dp))
    }
}

@Composable
private fun ProgressCard(progress: DailyDoseProgress?, onOpenAgenda: () -> Unit) {
    Column(Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)).background(Color.White, RoundedCornerShape(18.dp))
        .clickable(role = Role.Button, onClick = onOpenAgenda).padding(18.dp)) {
        Text("Progreso de hoy", fontFamily = homeInter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TataText)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { if (progress != null && progress.total > 0) progress.completed.toFloat() / progress.total else 0f },
                    modifier = Modifier.fillMaxSize(), color = Color(0xFF7D6BE5), trackColor = TataLavender, strokeWidth = 5.dp)
                Text(progress?.let { "${it.completed}/${it.total}" } ?: "—", fontFamily = homeInter, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TataNavy)
            }
            Column(Modifier.weight(1f).padding(start = 20.dp)) {
                Text("Tomas completadas", fontFamily = homeInter, fontSize = 14.sp, color = TataMuted)
                Text(when { progress == null -> "Consulta tu agenda"; progress.total == 0 -> "Sin tomas programadas"; progress.completed == progress.total -> "¡Muy bien, completaste tus tomas!"; else -> "Cada toma cuenta" },
                    fontFamily = homeInter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TataNavy, modifier = Modifier.padding(top = 4.dp))
            }
            Text("›", fontSize = 22.sp, color = TataNavy)
        }
    }
}

@Composable
private fun HomeShortcut(title: String, subtitle: String, color: Color, modifier: Modifier, onClick: (() -> Unit)?) {
    Column(modifier.shadow(5.dp, RoundedCornerShape(18.dp)).background(color, RoundedCornerShape(18.dp))
        .clickable(enabled = onClick != null, role = Role.Button, onClick = { onClick?.invoke() }).padding(16.dp)) {
        Text(title, fontFamily = homeInter, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = TataText)
        Text(subtitle, fontFamily = homeInter, fontSize = 12.sp, color = TataMuted, modifier = Modifier.padding(top = 5.dp))
    }
}

private fun currentDateLabel(): String =
    java.time.LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "PE")),
    ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }

private fun relativeTimeLabel(dose: NextDoseReadModel): String? {
    val minutes = Duration.between(java.time.Instant.now(), dose.scheduledAt).toMinutes()
    if (minutes < 0) return null
    return when {
        minutes < 60 -> "En $minutes min"
        minutes < 24 * 60 -> "En " + (minutes / 60) + " h"
        else -> null
    }
}
