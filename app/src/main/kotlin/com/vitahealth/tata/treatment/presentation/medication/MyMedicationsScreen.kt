package com.vitahealth.tata.treatment.presentation.medication

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val catalogInter = FontFamily(Font(R.font.tata_inter))
private val catalogSerif = FontFamily(Font(R.font.tata_serif))
private val catalogLavender = Color(0xFF5C63B2)

@Composable
fun MyMedicationsRoute(
    factory: MyMedicationsViewModel.Factory,
    onHome: () -> Unit,
    onAgenda: () -> Unit,
    onOpenDose: (String) -> Unit,
    onSignIn: () -> Unit,
    onNotes: (() -> Unit)? = null,
) {
    val model: MyMedicationsViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    MyMedicationsScreen(state, model::refresh, model::selectHistory, onHome, onAgenda, onOpenDose, onSignIn = onSignIn, onNotes = onNotes)
}

@Composable
fun MyMedicationsScreen(
    state: MyMedicationsUiState,
    onRetry: () -> Unit,
    onHistory: (Boolean) -> Unit,
    onHome: () -> Unit,
    onAgenda: () -> Unit,
    onOpenDose: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSignIn: (() -> Unit)? = null,
    onNotes: (() -> Unit)? = null,
) {
    var showAddHelp by rememberSaveable { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<MyMedication?>(null) }
    val active = state.medications.filter { it.active }
    val featured = active.firstOrNull { it.id == state.nextDose?.medicationId } ?: active.firstOrNull()
    val visible = state.medications.filter { it.active != state.history && (state.history || it.id != featured?.id) }
    Box(modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = 40.dp).size(260.dp)
            .background(Brush.radialGradient(listOf(Color(0x227D65C7), Color.Transparent))))
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                Spacer(Modifier.height(30.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.my_medications_title), Modifier.weight(1f), fontFamily = catalogSerif,
                        fontSize = 29.sp, lineHeight = 35.sp, color = TataText)
                    OutlinedIconButton(onClick = { showAddHelp = true }, modifier = Modifier.size(46.dp),
                        shape = CircleShape, border = BorderStroke(1.dp, Color(0xFFD7DAE4)), colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = Color.White)) {
                        Text("+", color = TataNavy, fontSize = 28.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.my_medications_subtitle), fontFamily = catalogInter, fontSize = 13.sp, lineHeight = 16.sp, color = TataNavy)
                    Spacer(Modifier.width(5.dp))
                    Box(Modifier.size(22.dp).background(Color(0xFF7887B2), CircleShape), contentAlignment = Alignment.Center) {
                        Text(active.size.toString(), color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                if (state.loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
                } else if (state.errorCode != null) {
                    val message = when(state.errorCode) {
                        "NETWORK_UNAVAILABLE" -> R.string.my_medications_offline
                        "SESSION_REQUIRED", "OLDER_ADULT_SESSION_REQUIRED" -> R.string.my_medications_session
                        else -> R.string.my_medications_error
                    }
                    Text(stringResource(message), color = TataText)
                    val sessionRequired = state.errorCode == "SESSION_REQUIRED" || state.errorCode == "OLDER_ADULT_SESSION_REQUIRED"
                    TextButton(onClick = if(sessionRequired && onSignIn != null) onSignIn else onRetry) {
                        Text(stringResource(if(sessionRequired && onSignIn != null) R.string.my_medications_sign_in else R.string.my_medications_retry))
                    }
                } else {
                    if (featured != null && !state.history) {
                        FeaturedMedication(featured, state, { selectedMedication = featured }, onOpenDose)
                        Spacer(Modifier.height(20.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CatalogFilter(stringResource(R.string.my_medications_active, active.size), !state.history) { onHistory(false) }
                        CatalogFilter(stringResource(R.string.my_medications_history), state.history) { onHistory(true) }
                    }
                    Spacer(Modifier.height(18.dp))
                    if (visible.isEmpty()) {
                        Text(stringResource(if(state.history) R.string.my_medications_history_empty else R.string.my_medications_empty), color = TataMuted)
                    }
                    visible.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEachIndexed { column, medication ->
                                MedicationTile(medication, rowIndex * 2 + column, Modifier.weight(1f)) { selectedMedication = medication }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            AdultTabBar(AdultTab.Medications, { tab ->
                when(tab) { AdultTab.Home -> onHome(); AdultTab.Agenda -> onAgenda(); AdultTab.Notes -> onNotes?.invoke(); else -> Unit }
            }, setOfNotNull(AdultTab.Home, AdultTab.Medications, AdultTab.Agenda, AdultTab.Notes.takeIf { onNotes != null }))
        }
    }
    if (showAddHelp) AlertDialog(onDismissRequest = { showAddHelp = false },
        title = { Text(stringResource(R.string.my_medications_add)) },
        text = { Text(stringResource(R.string.my_medications_add_help)) },
        confirmButton = { TextButton(onClick = { showAddHelp = false }) { Text(stringResource(R.string.my_medications_ok)) } })
    selectedMedication?.let { medication ->
        AlertDialog(onDismissRequest = { selectedMedication = null },
            title = { Text(medication.name) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(medication.presentation)
                Text(medication.dose.ifBlank { stringResource(R.string.my_medications_unconfigured) })
                if (medication.instructions.isNotBlank()) Text(medication.instructions)
                if (medication.scheduledTimes.isNotEmpty()) Text(stringResource(R.string.my_medications_schedule, medication.scheduledTimes.joinToString()))
            } },
            confirmButton = { TextButton(onClick = { selectedMedication = null }) { Text(stringResource(R.string.my_medications_ok)) } })
    }
}

@Composable
private fun CatalogFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.background(if(selected) TataNavy else Color(0xFFE8EBF0), RoundedCornerShape(15.dp))
        .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 8.dp)) {
        Text(label, fontFamily = catalogInter, fontSize = 12.sp, lineHeight = 15.sp, color = if(selected) Color.White else TataMuted)
    }
}

@Composable
private fun FeaturedMedication(medication: MyMedication, state: MyMedicationsUiState, onDetail: () -> Unit, onOpenDose: (String) -> Unit) {
    val nextDose = state.nextDose?.takeIf { it.medicationId == medication.id }
    val shape = RoundedCornerShape(20.dp)
    Box(Modifier.fillMaxWidth().shadow(6.dp, shape, ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138)).background(
        Brush.horizontalGradient(listOf(Color(0xFFF0EAFF), Color(0xFFE5DCF9))), shape).padding(14.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(end = 108.dp).clickable(onClick = onDetail)) {
                Text(stringResource(R.string.my_medications_featured) + "  ›", color = catalogLavender, fontFamily = catalogInter, fontSize = 13.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text(medication.name, color = TataText, fontFamily = catalogInter, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 29.sp)
                Spacer(Modifier.height(4.dp))
                Text(medication.dose.ifBlank { medication.presentation }, color = TataText, fontFamily = catalogInter, fontSize = 13.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TataSvgIcon(R.raw.my_medications_morning, Modifier.size(16.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(catalogLavender))
                    Spacer(Modifier.width(5.dp))
                    Text(scheduleLabel(medication), color = catalogLavender, fontFamily = catalogInter, fontSize = 13.sp, lineHeight = 16.sp)
                }
            }
            if (nextDose != null) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp))
                    .clickable { onOpenDose(nextDose.intakeId) }.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.my_medications_next_dose), color = TataMuted, fontFamily = catalogInter, fontSize = 11.sp, lineHeight = 13.sp)
                        Text(nextDose.scheduledAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-PE"))),
                            color = TataText, fontWeight = FontWeight.Bold, fontFamily = catalogInter, fontSize = 19.sp, lineHeight = 23.sp)
                    }
                    Text("›", color = TataNavy, fontSize = 20.sp)
                }
            }
        }
        Image(painterResource(R.drawable.my_medications_tablet), null, Modifier.align(Alignment.TopEnd).offset(x = 17.dp, y = (-10).dp).size(142.dp))
    }
}

@Composable
private fun MedicationTile(medication: MyMedication, index: Int, modifier: Modifier, onClick: () -> Unit) {
    val colors = listOf(listOf(0xFFFFF4E2, 0xFFFBE7C9), listOf(0xFFF1ECFF, 0xFFE7DFF9), listOf(0xFFECF7EF, 0xFFE1F0E5), listOf(0xFFECF5FB, 0xFFDFECF6))
    val shape = RoundedCornerShape(16.dp)
    val night = medication.scheduledTimes.firstOrNull()?.let { runCatching { LocalTime.parse(it).hour >= 18 }.getOrDefault(false) } ?: false
    Column(modifier.heightIn(min = 98.dp).shadow(6.dp, shape, ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138)).background(Brush.horizontalGradient(colors[index % colors.size].map { Color(it) }), shape)
        .clickable(onClick = onClick).padding(14.dp)) {
        Text(medication.name, fontFamily = catalogInter, fontSize = 12.5.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, color = TataText)
        Text(medication.dose.ifBlank { medication.presentation }, fontFamily = catalogInter, fontSize = 12.sp, lineHeight = 15.sp, color = TataText)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TataSvgIcon(if(night) R.raw.my_medications_night else R.raw.my_medications_morning, Modifier.size(22.dp))
            Spacer(Modifier.width(2.dp))
            Text(scheduleLabel(medication), fontFamily = catalogInter, fontSize = 12.sp, lineHeight = 15.sp,
                color = if(night) catalogLavender else Color(0xFFC48C29))
        }
    }
}

private fun scheduleLabel(medication: MyMedication): String =
    medication.instructions.ifBlank { medication.scheduledTimes.joinToString(" · ") }
