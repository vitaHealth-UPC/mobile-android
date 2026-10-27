package com.vitahealth.tata.app.integration.linkedperson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.presentation.contact.mappers.openContact
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.*
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val serif = FontFamily(Font(R.font.tata_serif))
private val inter = FontFamily(Font(R.font.tata_inter))
private val navy = Color(0xFF0E2952)

@Composable
fun LinkedPersonRoute(
    factory: LinkedPersonViewModel.Factory,
    onBack: () -> Unit,
    onMedication: () -> Unit,
    onHistory: () -> Unit,
    onTreatments: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    val vm: LinkedPersonViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var firstResume by remember(vm) { mutableStateOf(true) }
    DisposableEffect(owner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (firstResume) firstResume = false else vm.refresh()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.pendingContact) {
        state.pendingContact?.let { vm.contactHandled(context.openContact(it)) }
    }
    LinkedPersonScreen(
        state,
        onBack,
        vm::refresh,
        { vm.contact(ContactChannelType.PHONE) },
        { vm.contact(ContactChannelType.WHATSAPP) },
        onMedication,
        onHistory,
        onTreatments,
        onTabSelected,
        onEdit = vm::beginEdit,
    )
    if (state.editing) {
        LinkedPersonEditDialog(
            state = state,
            onName = vm::setEditName,
            onBirthDate = vm::setEditBirthDate,
            onCancel = vm::cancelEdit,
            onSave = vm::saveEdit,
            onContactEdit = vm::beginContactEdit,
            onContactField = vm::setContactField,
        )
    }
}

/** Figma #16. All clinical figures and actions come from authorized read models. */
@Composable
fun LinkedPersonScreen(
    state: LinkedPersonUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onMedication: () -> Unit,
    onHistory: () -> Unit,
    onTreatments: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
    onEdit: () -> Unit = {},
) {
    val data = state.snapshot
    Column(Modifier.fillMaxSize().background(tataBackgroundColor())) {
        Column(
            Modifier.weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "‹",
                    modifier =
                        Modifier.clickable(role = Role.Button, onClick = onBack)
                            .padding(end = 18.dp),
                    fontSize = 32.sp,
                    color = tataTextColor(),
                )
                Text(
                    data?.person?.fullName ?: "Persona vinculada",
                    modifier = Modifier.weight(1f),
                    fontFamily = serif,
                    fontSize = 24.sp,
                    color = tataTextColor(),
                )
                Text(
                    "Editar",
                    fontSize = 11.sp,
                    color = tataMutedColor(),
                    modifier =
                        Modifier.background(tataTonalColor(Color.White), RoundedCornerShape(50))
                            .clickable(
                                enabled = data != null && !state.loading,
                                role = Role.Button,
                                onClickLabel = "Editar perfil",
                                onClick = onEdit,
                            )
                            .padding(horizontal = 13.dp, vertical = 9.dp),
                )
            }
            when {
                state.loading -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Consultando perfil y consentimiento…", color = tataMutedColor())
                }
                data == null ->
                    TataCard(Modifier.fillMaxWidth(), tataTonalColor(TataLavender)) {
                        Text(state.error ?: "Ficha no disponible", color = tataTextColor())
                        TataButton(
                            if (state.accessDenied) "Volver" else "Reintentar",
                            if (state.accessDenied) onBack else onRetry,
                        )
                    }
                else -> {
                    val person = data.person
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(17.dp),
                    ) {
                        Box(
                            Modifier.size(98.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            tataTonalColor(TataLavender),
                                            tataTonalColor(TataMint),
                                        )
                                    ),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                person.fullName
                                    .trim()
                                    .split(Regex("\\s+"))
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .joinToString(""),
                                fontFamily = serif,
                                fontSize = 32.sp,
                                color = tataTextColor(),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            val birth = person.birthDate
                            Text(
                                birth?.let {
                                    Period.between(it, LocalDate.now()).years.toString() + " años"
                                } ?: "Edad no registrada",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = tataTextColor(),
                            )
                            Text(
                                birth
                                    ?.format(
                                        DateTimeFormatter.ofPattern(
                                            "d MMM",
                                            Locale.forLanguageTag("es-PE"),
                                        )
                                    )
                                    ?.let { "Cumpleaños: $it" } ?: "Cumpleaños no registrado",
                                fontSize = 12.sp,
                                color = tataMutedColor(),
                            )
                            Text(
                                "Vinculada",
                                fontSize = 11.sp,
                                color = Color(0xFF3D754F),
                                modifier =
                                    Modifier.background(
                                            tataTonalColor(TataMint),
                                            RoundedCornerShape(50),
                                        )
                                        .padding(horizontal = 11.dp, vertical = 5.dp),
                            )
                            Text(
                                data.linkedAt
                                    ?.atZone(ZoneId.systemDefault())
                                    ?.format(
                                        DateTimeFormatter.ofPattern(
                                            "MMM yyyy",
                                            Locale.forLanguageTag("es-PE"),
                                        )
                                    )
                                    ?.let { "Desde $it" } ?: "Fecha de vínculo no disponible",
                                fontSize = 11.sp,
                                color = tataMutedColor(),
                            )
                        }
                    }
                    val weekly = data.summary.weekly
                    TataCard(Modifier.fillMaxWidth(), tataTonalColor(Color(0xFFECE6FB))) {
                        Text(
                            "Nivel de adherencia  ⓘ",
                            fontFamily = inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = tataTextColor(),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = {
                                        ((weekly.percentage ?: 0.0) / 100.0)
                                            .toFloat()
                                            .coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.size(56.dp),
                                    strokeWidth = 5.dp,
                                    color = Color(0xFF3D754F),
                                    trackColor = Color(0xFFDAD5E7),
                                )
                                Text(
                                    weekly.percentage?.roundToInt()?.let { "$it%" } ?: "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tataTextColor(),
                                )
                            }
                            Column {
                                Text(
                                    if (weekly.total == 0) "Sin datos suficientes"
                                    else "Adherencia semanal",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tataTextColor(),
                                )
                                Text(
                                    if (weekly.total == 0) "Aún no hay tomas resueltas"
                                    else
                                        weekly.confirmed.toString() +
                                            " de " +
                                            weekly.total +
                                            " tomas confirmadas",
                                    fontSize = 11.sp,
                                    color = tataMutedColor(),
                                )
                            }
                        }
                    }
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(
                            "Tratamiento actual",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = tataTextColor(),
                        )
                        val active = data.treatments.filter { it.status == TreatmentStatus.ACTIVE }
                        val paused = data.treatments.count { it.status == TreatmentStatus.PAUSED }
                        Text(
                            active.size.toString() +
                                " tratamientos activos" +
                                if (paused > 0) " · $paused pausados" else "",
                            fontSize = 11.sp,
                            color = tataMutedColor(),
                        )
                        if (active.isEmpty())
                            Text(
                                "No hay tratamientos activos.",
                                fontSize = 12.sp,
                                color = tataMutedColor(),
                            )
                        active.take(4).chunked(2).forEach { pair ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                pair.forEach { treatment ->
                                    Row(
                                        Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            Modifier.size(7.dp)
                                                .background(Color(0xFF3D754F), CircleShape)
                                        )
                                        Text(
                                            treatment.name,
                                            Modifier.padding(start = 7.dp),
                                            fontSize = 11.sp,
                                            color = tataTextColor(),
                                            maxLines = 2,
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                        Text(
                            "Ver tratamientos",
                            Modifier.clickable(role = Role.Button, onClick = onTreatments)
                                .padding(vertical = 5.dp),
                            fontSize = 12.sp,
                            color = navy,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuickLink(
                            R.raw.tata_linked_call,
                            "Llamar",
                            Color(0xFFE5F2E8),
                            !state.contactBusy,
                            onCall,
                            Modifier.weight(1f),
                        )
                        QuickLink(
                            R.raw.tata_linked_message,
                            "Mensajes",
                            Color(0xFFE8E1FC),
                            !state.contactBusy,
                            onMessage,
                            Modifier.weight(1f),
                        )
                        QuickLink(
                            R.raw.tata_linked_medication,
                            "Ver\nmedicación",
                            Color(0xFFFBE9D3),
                            true,
                            onMedication,
                            Modifier.weight(1f),
                        )
                        QuickLink(
                            R.raw.tata_linked_history,
                            "Historial",
                            Color(0xFFDFECF6),
                            true,
                            onHistory,
                            Modifier.weight(1f),
                        )
                    }
                    state.contactError?.let { Text(it, fontSize = 12.sp, color = tataMutedColor()) }
                }
            }
        }
        CaregiverTabBar(CaregiverTab.Person, onTabSelected)
    }
}

@Composable
private fun LinkedPersonEditDialog(
    state: LinkedPersonUiState,
    onName: (String) -> Unit,
    onBirthDate: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onContactEdit: () -> Unit,
    onContactField: (String, String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!state.editSaving) onCancel() },
        title = {
            Text(if (state.editingContact) "Contacto de emergencia" else "Editar perfil vinculado")
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.editingContact) {
                    Text(
                        "Ingresa los datos completos del contacto que deseas registrar.",
                        fontSize = 12.sp,
                    )
                    OutlinedTextField(
                        value = state.contactName,
                        onValueChange = { onContactField("name", it) },
                        modifier = Modifier.testTag("linked-contact-name"),
                        label = { Text("Nombre del contacto") },
                        enabled = !state.editSaving,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.contactRelationship,
                        onValueChange = { onContactField("relationship", it) },
                        modifier = Modifier.testTag("linked-contact-relationship"),
                        label = { Text("Relación") },
                        enabled = !state.editSaving,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.contactPhone,
                        onValueChange = { onContactField("phone", it) },
                        modifier = Modifier.testTag("linked-contact-phone"),
                        label = { Text("Teléfono") },
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            ),
                        enabled = !state.editSaving,
                        singleLine = true,
                    )
                } else {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = onName,
                        modifier = Modifier.testTag("linked-person-name"),
                        label = { Text("Nombre completo") },
                        enabled = !state.editSaving,
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.editBirthDate,
                        onValueChange = onBirthDate,
                        modifier = Modifier.testTag("linked-person-birth-date"),
                        label = { Text("Fecha de nacimiento (AAAA-MM-DD)") },
                        enabled = !state.editSaving,
                        singleLine = true,
                    )
                    TextButton(onClick = onContactEdit, enabled = !state.editSaving) {
                        Text("Editar contacto de emergencia")
                    }
                }
                state.editError?.let {
                    Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !state.editSaving) {
                if (state.editSaving) CircularProgressIndicator(Modifier.size(18.dp))
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !state.editSaving) { Text("Cancelar") }
        },
    )
}

@Composable
private fun QuickLink(
    iconRes: Int,
    label: String,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tataTonalColor(background))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = label.replace("\n", " "),
                onClick = onClick,
            )
            .height(77.dp)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TataSvgIcon(iconRes, Modifier.size(24.dp))
        Text(
            label,
            fontSize = 10.sp,
            lineHeight = 11.sp,
            fontFamily = inter,
            textAlign = TextAlign.Center,
            color = tataTextColor(),
        )
    }
}
