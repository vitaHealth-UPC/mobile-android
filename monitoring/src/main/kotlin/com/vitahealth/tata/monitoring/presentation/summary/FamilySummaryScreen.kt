package com.vitahealth.tata.monitoring.presentation.summary

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.R
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val summaryLocale = Locale("es", "PE")

@Composable
fun FamilySummaryRoute(factory: FamilySummaryViewModel.Factory, olderAdultName: String,
    onAgenda: () -> Unit, onHistory: () -> Unit, onAddMedication: () -> Unit, onChangePerson: () -> Unit,
    onAccessibility: () -> Unit = {}, onNotificationPreferences: () -> Unit = {},
    onMedications: () -> Unit = {}, onTreatments: () -> Unit = {}) {
    val model: FamilySummaryViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) model.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    FamilySummaryScreen(state, olderAdultName, model::refresh, onAgenda, onHistory, model::contact,
        model::alerts, model::notes, onAddMedication, onChangePerson, onAccessibility, onNotificationPreferences, onMedications, onTreatments)
    state.dialog?.let { dialog ->
        val context = LocalContext.current
        AlertDialog(onDismissRequest = model::dismissDialog, title = { Text(dialog.title) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                if (dialog.loading) Text("Consultando…")
                else if (dialog.rows.isEmpty()) Text("No hay registros para mostrar.")
                else dialog.rows.forEach { Text(it, modifier = Modifier.padding(bottom = 16.dp)) }
            } }, confirmButton = {
                TextButton(onClick = {
                    if (dialog.phone != null) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", dialog.phone, null))
                        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                    }
                    model.dismissDialog()
                }) { Text(if (dialog.phone == null) "Cerrar" else "Abrir teléfono") }
            })
    }
}

@Composable
fun FamilySummaryScreen(state: FamilySummaryUiState, olderAdultName: String, onRetry: () -> Unit,
    onAgenda: () -> Unit, onHistory: () -> Unit, onContact: () -> Unit, onAlerts: () -> Unit,
    onNotes: () -> Unit, onAddMedication: () -> Unit, onChangePerson: () -> Unit,
    onAccessibility: () -> Unit = {}, onNotificationPreferences: () -> Unit = {},
    onMedications: () -> Unit = {}, onTreatments: () -> Unit = {}) {
    var more by remember { mutableStateOf(false) }
    val summary = state.summary
    Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF))))) {
        Box(Modifier.weight(1f)) {
            Image(painterResource(R.drawable.family_glow), null, Modifier.align(Alignment.TopEnd).offset(x = 72.dp, y = 4.dp).size(386.dp))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                Spacer(Modifier.height(28.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Resumen familiar", fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
                            fontSize = 29.sp, color = TataText)
                        TextButton(onClick = onChangePerson, contentPadding = PaddingValues(0.dp)) {
                            Text(olderAdultName.substringBefore(" ") + " ⌄", color = TataDeepNavy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(Modifier.size(48.dp).clip(CircleShape).background(TataLavender), contentAlignment = Alignment.Center) {
                        Text(olderAdultName.take(1).uppercase(summaryLocale), color = TataDeepNavy, fontSize = 20.sp)
                    }
                    Box(Modifier.size(48.dp).clickable(role = Role.Button, onClickLabel = "Ver alertas", onClick = onAlerts), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.family_bell), null, Modifier.requiredSize(100.dp).offset(y = 14.dp))
                        Image(painterResource(R.drawable.family_header_bell), "Ver alertas", Modifier.size(22.dp))
                    }
                }
                when {
                    state.loading -> TataCard(Modifier.fillMaxWidth(), TataMint) { Text("Consultando el resumen…", color = TataText) }
                    state.error != null -> TataCard(Modifier.fillMaxWidth()) {
                        Text(state.error, color = TataText)
                        TataButton("Reintentar", onRetry, Modifier.padding(top = 12.dp))
                    }
                    summary != null -> {
                        SummaryCard(listOf(TataMint, Color(0xFFDDEDE1))) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Adherencia esta semana", fontSize = 12.sp, color = Color(0xFF296345))
                                    Text(summary.weekly.percentage?.let { "${it.roundToInt()}%" } ?: "Sin datos", fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold, color = TataText, modifier = Modifier.padding(top = 6.dp))
                                    Text(if (summary.weekly.total == 0) "Aún no hay tomas resueltas" else "${summary.weekly.confirmed} de ${summary.weekly.total} tomas confirmadas",
                                        fontSize = 12.sp, color = TataText, modifier = Modifier.padding(top = 2.dp))
                                }
                                Image(painterResource(R.drawable.family_gauge), null, Modifier.size(112.dp, 84.dp))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        SummaryCard(listOf(Color.White, Color.White)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Hoy", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TataText)
                                    Text(summary.date.format(DateTimeFormatter.ofPattern("d 'de' MMMM, EEEE", summaryLocale)), fontSize = 11.sp, color = TataMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${summary.today.confirmed} / ${summary.today.total}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TataText)
                                    Text("dosis completadas", fontSize = 10.sp, color = TataMuted)
                                }
                            }
                            LinearProgressIndicator(progress = { ((summary.today.percentage ?: 0.0) / 100).toFloat() },
                                color = Color(0xFF75A985), trackColor = Color(0xFFE4E5EC),
                                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().height(6.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        SummaryCard(listOf(TataLavender, Color(0xFFE7DFFC)), onAgenda) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Próxima dosis", color = TataMuted, fontSize = 11.sp)
                                    Text(summary.nextDose?.medicationName ?: "No hay próximas tomas", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TataText)
                                    summary.nextDose?.let { Text(it.dose, fontSize = 11.sp, color = TataText) }
                                }
                                summary.nextDose?.let { Text(it.scheduledAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a", summaryLocale)),
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TataText) }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        SummaryCard(listOf(TataCream, Color(0xFFFBE9C8))) {
                            Text("Atención", fontSize = 11.sp, color = Color(0xFFC48C29))
                            Text(summary.stockAttention.firstOrNull()?.let { "Quedan ${it.remaining} unidades de ${it.medicationName}." }
                                ?: if (summary.inventoryAvailable) "No hay alertas de reposición." else "No pudimos consultar el inventario.",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TataText)
                            Text(if (summary.stockAttention.isEmpty()) "Revisa los medicamentos y sus cantidades." else "Considera solicitar tu reabastecimiento.", fontSize = 11.sp, color = TataMuted)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("Monitoreo rápido", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TataText)
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            QuickAction("Ver agenda", R.drawable.family_agenda, TataMint, onAgenda, Modifier.weight(1f))
                            QuickAction("Historial", R.drawable.family_history, TataLavender, onHistory, Modifier.weight(1f))
                            QuickAction("Contactar", R.drawable.family_contact, Color(0xFFECF5FB), onContact, Modifier.weight(1f))
                        }
                        TextButton(onClick = onAddMedication, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Agregar medicamento", color = TataDeepNavy) }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth().background(Color.White, RoundedCornerShape(28.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) {
            listOf(Triple("Inicio", R.drawable.family_home, onRetry), Triple("Alertas", R.drawable.family_alerts, onAlerts),
                Triple("Notas", R.drawable.family_notes, onNotes), Triple("Persona", R.drawable.family_person, onChangePerson),
                Triple("Más", R.drawable.family_more, { more = true })).forEachIndexed { index, item ->
                Column(Modifier.widthIn(min = 48.dp).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = item.third), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(38.dp, 30.dp).background(if (index == 0) TataNavy else Color.Transparent, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Image(painterResource(item.second), null, Modifier.size(22.dp))
                    }
                    Text(item.first, fontSize = 9.sp, color = if (index == 0) TataNavy else TataMuted)
                }
            }
        }
    }
    if (more) AlertDialog(onDismissRequest = { more = false }, title = { Text("Más opciones") }, text = {
        Column { TextButton(onClick = { more = false; onTreatments() }) { Text("Tratamientos") }
            TextButton(onClick = { more = false; onMedications() }) { Text("Medicamentos") }
            TextButton(onClick = { more = false; onAddMedication() }) { Text("Agregar medicamento") }
            TextButton(onClick = { more = false; onAccessibility() }) { Text("Accesibilidad") }
            TextButton(onClick = { more = false; onNotificationPreferences() }) { Text("Preferencias de notificación") }
            TextButton(onClick = { more = false; onChangePerson() }) { Text("Vincular otra persona") }
            TextButton(onClick = { more = false; onRetry() }) { Text("Actualizar resumen") } }
    }, confirmButton = { TextButton(onClick = { more = false }) { Text("Cerrar") } })
}

@Composable private fun SummaryCard(colors: List<Color>, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val click = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(colors)).then(click).padding(16.dp), content = content)
}
@Composable private fun QuickAction(label: String, icon: Int, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(color).clickable(role = Role.Button, onClick = onClick).padding(vertical = 19.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(icon), null, Modifier.size(22.dp))
        Text(label, fontSize = 12.sp, color = TataText, modifier = Modifier.padding(top = 10.dp))
    }
}
