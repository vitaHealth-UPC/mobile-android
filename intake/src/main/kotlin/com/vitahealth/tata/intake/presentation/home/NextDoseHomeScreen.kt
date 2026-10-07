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
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.components.AdultTab
import com.vitahealth.tata.shared.design.components.AdultTabBar
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
    val locale = Locale.forLanguageTag("es-PE")
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
    var showTip by rememberSaveable { mutableStateOf(true) }
    Column(modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_today), fontFamily = homeSerif, fontSize = 34.sp, color = TataText)
                    Text(currentDateLabel(locale), fontFamily = homeInter, fontSize = 13.sp, lineHeight = 16.sp, color = TataMuted)
                }
                if (name.isNotBlank()) Box(Modifier.size(44.dp).background(TataLavender, CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
                        fontFamily = homeInter, color = TataNavy, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            when (state) {
                NextDoseHomeUiState.Loading -> TataCard(Modifier.fillMaxWidth(), TataLavender) { Text(stringResource(R.string.home_loading), color = TataMuted) }
                is NextDoseHomeUiState.NextDoseAvailable -> NextDoseCard(state.dose, onOpenDoseDetail)
                is NextDoseHomeUiState.NoNextDose -> TataCard(Modifier.fillMaxWidth(), TataLavender) {
                    Text(stringResource(R.string.home_empty_title), color = TataNavy, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.home_empty_description), color = TataText, modifier = Modifier.padding(top = 12.dp))
                }
                is NextDoseHomeUiState.Error -> TataCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_error_title), color = TataText, fontWeight = FontWeight.Bold)
                    Text(state.message, color = TataMuted, modifier = Modifier.padding(top = 8.dp))
                    TataButton(stringResource(R.string.home_retry), onRetry, Modifier.padding(top = 12.dp))
                }
            }
            Spacer(Modifier.height(19.dp))
            ProgressCard(progress, onOpenAgenda)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                // Voice recognition has no route yet; do not pretend that a tap confirms an intake.
                HomeShortcut(stringResource(R.string.home_voice_title), stringResource(R.string.home_voice_subtitle), TataMint, com.vitahealth.tata.shared.R.raw.home_mic, Modifier.weight(1f), null)
                HomeShortcut(stringResource(R.string.home_agenda_title), stringResource(R.string.home_agenda_subtitle), Color(0xFFE1EFF8), com.vitahealth.tata.shared.R.raw.home_list, Modifier.weight(1f), onOpenAgenda)
            }
            if (showTip) {
                Spacer(Modifier.height(21.dp))
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFFFF4E2), Color(0xFFFBE9CD))), RoundedCornerShape(18.dp)).padding(16.dp)) {
                    Text("☼", fontSize = 20.sp, color = Color(0xFFC48C29), modifier = Modifier.padding(end = 14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.home_tip_title), fontFamily = homeInter, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC48C29))
                        Text(stringResource(R.string.home_tip_description), fontFamily = homeInter, fontSize = 14.sp, lineHeight = 18.sp, color = TataText, modifier = Modifier.padding(top = 7.dp))
                    }
                    val dismissTip = stringResource(R.string.home_dismiss_tip)
                    IconButton(onClick = { showTip = false }, modifier = Modifier.size(24.dp).semantics { contentDescription = dismissTip }) { Text("×", color = TataMuted) }
                }
            }
            onSignOut?.let { action -> TextButton(onClick = action) { Text(stringResource(R.string.home_switch_account), color = TataNavy) } }
            Spacer(Modifier.height(24.dp))
        }
        AdultTabBar(
            selected = AdultTab.Home,
            availableTabs = setOf(AdultTab.Home, AdultTab.Agenda),
            onSelect = { if (it == AdultTab.Agenda) onOpenAgenda() },
        )
    }
}

@Composable
private fun NextDoseCard(dose: NextDoseReadModel, onOpenDoseDetail: (String) -> Unit) {
    val locale = Locale.forLanguageTag("es-PE")
    Row(Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
        .background(Brush.horizontalGradient(listOf(Color(0xFFEEE8FF), Color(0xFFE5DDF8))))
        .clickable(role = Role.Button) { onOpenDoseDetail(dose.id) }.padding(start = 18.dp, end = 14.dp, top = 18.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.home_next_dose), fontFamily = homeInter, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = TataNavy)
            Text(dose.scheduledAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a", locale)).lowercase().replace("am", "a. m.").replace("pm", "p. m."),
                fontFamily = homeInter, fontSize = 31.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, color = TataText, modifier = Modifier.padding(top = 12.dp))
            Text(dose.medicationName, fontFamily = homeInter, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold, color = TataText, modifier = Modifier.padding(top = 4.dp))
            Text(dose.dose, fontFamily = homeInter, fontSize = 13.sp, lineHeight = 16.sp, color = TataMuted, modifier = Modifier.padding(top = 3.dp))
            relativeTimeLabel(dose)?.let { remaining ->
                val label = stringResource(if (remaining.hours) R.string.home_in_hours else R.string.home_in_minutes, remaining.value)
                Text(label, fontFamily = homeInter, fontSize = 12.sp, lineHeight = 15.sp, color = Color(0xFF5961B2), modifier = Modifier.padding(top = 4.dp).background(Color.White, CircleShape).padding(horizontal = 18.dp, vertical = 4.dp))
            }
        }
        Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.home_tablet), null, Modifier.requiredSize(176.dp).offset(y = 12.dp))
        }
    }
}

@Composable
private fun ProgressCard(progress: DailyDoseProgress?, onOpenAgenda: () -> Unit) {
    Column(Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)).background(Color.White, RoundedCornerShape(18.dp))
        .clickable(role = Role.Button, onClick = onOpenAgenda).padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp)) {
        Text(stringResource(R.string.home_progress_title), fontFamily = homeInter, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, color = TataText)
        Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { if (progress != null && progress.total > 0) progress.completed.toFloat() / progress.total else 0f },
                    modifier = Modifier.fillMaxSize(), color = Color(0xFF7D6BE5), trackColor = TataLavender, strokeWidth = 5.dp)
                Text(progress?.let { "${it.completed}/${it.total}" } ?: "—", fontFamily = homeInter, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold, color = TataNavy)
            }
            Column(Modifier.weight(1f).padding(start = 20.dp)) {
                Text(stringResource(R.string.home_completed), fontFamily = homeInter, fontSize = 14.sp, lineHeight = 17.sp, color = TataMuted)
                Text(when { progress == null -> stringResource(R.string.home_progress_unavailable); progress.total == 0 -> stringResource(R.string.home_progress_empty); progress.completed == progress.total -> stringResource(R.string.home_progress_done); else -> stringResource(R.string.home_progress_encouragement) },
                    fontFamily = homeInter, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, color = TataNavy, modifier = Modifier.padding(top = 4.dp))
            }
            Text("›", fontSize = 22.sp, color = TataNavy)
        }
    }
}

@Composable
private fun HomeShortcut(title: String, subtitle: String, color: Color, icon: Int, modifier: Modifier, onClick: (() -> Unit)?) {
    Column(modifier.shadow(5.dp, RoundedCornerShape(18.dp)).background(color, RoundedCornerShape(18.dp))
        .clickable(enabled = onClick != null, role = Role.Button, onClick = { onClick?.invoke() }).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontFamily = homeInter, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = TataText, modifier = Modifier.weight(1f))
            TataSvgIcon(icon, Modifier.size(if (icon == com.vitahealth.tata.shared.R.raw.home_list) 27.dp else 28.dp))
        }
        Text(subtitle, fontFamily = homeInter, fontSize = 12.sp, lineHeight = 15.sp, color = TataMuted, modifier = Modifier.padding(top = 2.dp))
    }
}

private fun currentDateLabel(locale: Locale): String =
    java.time.LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale),
    ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

private data class RemainingTime(val value: Long, val hours: Boolean = false)

private fun relativeTimeLabel(dose: NextDoseReadModel): RemainingTime? {
    val minutes = Duration.between(java.time.Instant.now(), dose.scheduledAt).toMinutes()
    if (minutes < 0) return null
    return when {
        minutes < 60 -> RemainingTime(minutes)
        minutes < 24 * 60 -> RemainingTime(minutes / 60, hours = true)
        else -> null
    }
}
