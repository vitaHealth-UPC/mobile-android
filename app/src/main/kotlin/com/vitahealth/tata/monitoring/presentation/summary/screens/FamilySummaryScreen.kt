package com.vitahealth.tata.monitoring.presentation.summary.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.presentation.summary.ui.QuickAction
import com.vitahealth.tata.monitoring.presentation.summary.ui.SummaryCard
import com.vitahealth.tata.monitoring.presentation.summary.ui.summaryLocale
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


@Composable
fun FamilySummaryScreen(
    state: FamilySummaryUiState,
    olderAdultName: String,
    onRetry: () -> Unit,
    onAgenda: () -> Unit,
    onHistory: () -> Unit,
    onContact: () -> Unit,
    onAlerts: () -> Unit,
    onNotes: () -> Unit,
    onAddMedication: () -> Unit,
    onChangePerson: () -> Unit,
    onAccessibility: () -> Unit = {},
    onNotificationPreferences: () -> Unit = {},
    onMedications: () -> Unit = {},
    onTreatments: () -> Unit = {},
    onSubscription: () -> Unit = {},
) {
    var more by remember { mutableStateOf(false) }
    val summary = state.summary
    val darkTheme = LocalTataAccessibility.current.darkTheme
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
                    if (darkTheme) {
                        Brush.linearGradient(
                            listOf(tataBackgroundColor(), tataBackgroundColor())
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color.White, TataSurface, Color(0xFFFCFBFF))
                        )
                    }
                )
        ) {
            Box(Modifier.weight(1f)) {
                // The pastel glow belongs to Figma's light surface. Dark mode keeps
                // the content hierarchy without a large opaque decorative circle.
                if (!darkTheme) {
                    TataSvgIcon(
                        R.raw.family_glow,
                        Modifier.align(Alignment.TopEnd)
                            .offset(x = 72.dp, y = 4.dp)
                            .size(386.dp),
                    )
                }
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                ) {
                    Spacer(Modifier.height(28.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Resumen familiar",
                                fontFamily = FontFamily(Font(R.font.tata_serif)),
                                fontSize = 29.sp,
                                lineHeight = 34.sp,
                                color = tataTextColor(),
                            )
                            TextButton(
                                onClick = onChangePerson,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    olderAdultName.substringBefore(" ") + " ⌄",
                                    color = tataAccentColor(TataDeepNavy),
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(tataTonalColor(TataLavender)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                olderAdultName.take(1).uppercase(summaryLocale),
                                color = tataAccentColor(TataDeepNavy),
                                fontSize = 20.sp,
                                lineHeight = 24.sp,
                            )
                        }
                        Box(
                            Modifier.size(48.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "Ver alertas",
                                    onClick = onAlerts,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!darkTheme) {
                                TataSvgIcon(
                                    R.raw.family_bell,
                                    Modifier.requiredSize(100.dp).offset(y = 14.dp),
                                )
                            } else {
                                Box(
                                    Modifier.size(44.dp)
                                        .background(
                                            tataTonalColor(TataLavender),
                                            CircleShape,
                                        ),
                                )
                            }
                            TataSvgIcon(
                                R.raw.family_header_bell,
                                Modifier.size(22.dp),
                                colorFilter =
                                    if (darkTheme) ColorFilter.tint(tataAccentColor(TataDeepNavy))
                                    else null,
                            )
                        }
                    }
                    when {
                        state.loading ->
                            TataCard(Modifier.fillMaxWidth(), tataTonalColor(TataMint)) {
                                Text("Consultando el resumen…", color = tataTextColor())
                            }
                        state.errorCode == "CARE_RELATIONSHIP_REQUIRED" -> {
                            Spacer(Modifier.height(124.dp))
                            Box(
                                Modifier.fillMaxWidth()
                                    .heightIn(min = 226.dp)
                                    .background(tataTonalColor(TataCream), RoundedCornerShape(20.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Seguimiento restringido",
                                        fontSize = 21.sp,
                                        lineHeight = 27.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = tataTextColor(),
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        "Se requiere un vínculo activo y el consentimiento de ${olderAdultName.substringBefore(' ')} para compartir su información.",
                                        color = tataMutedColor(),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                        state.error != null ->
                            TataCard(Modifier.fillMaxWidth()) {
                                Text(state.error, color = tataTextColor())
                                TataButton("Reintentar", onRetry, Modifier.padding(top = 12.dp))
                            }
                        summary != null -> {
                            SummaryCard(listOf(TataMint, Color(0xFFDDEDE1))) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Adherencia esta semana",
                                            fontSize = 12.sp,
                                            color = tataAccentColor(Color(0xFF296345)),
                                        )
                                        Text(
                                            summary.weekly.percentage?.let { "${it.roundToInt()}%" }
                                                ?: "Sin datos",
                                            fontSize = 30.sp,
                                            lineHeight = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tataTextColor(),
                                            modifier = Modifier.padding(top = 6.dp),
                                        )
                                        Text(
                                            if (summary.weekly.total == 0)
                                                "Aún no hay tomas resueltas"
                                            else
                                                "${summary.weekly.confirmed} de ${summary.weekly.total} tomas confirmadas",
                                            fontSize = 12.sp,
                                            color = tataTextColor(),
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                    if (darkTheme) {
                                        // Real adherence data, not a substitute sample illustration.
                                        CircularProgressIndicator(
                                            progress = {
                                                ((summary.weekly.percentage ?: 0.0) / 100.0)
                                                    .toFloat()
                                                    .coerceIn(0f, 1f)
                                            },
                                            modifier = Modifier.size(76.dp),
                                            color = Color(0xFF80BF99),
                                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                                            strokeWidth = 7.dp,
                                        )
                                    } else {
                                        Image(
                                            painterResource(R.drawable.family_gauge_high_res),
                                            null,
                                            Modifier.size(112.dp, 84.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            SummaryCard(listOf(Color.White, Color.White)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Hoy",
                                            fontSize = 16.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = tataTextColor(),
                                        )
                                        Text(
                                            summary.date.format(
                                                DateTimeFormatter.ofPattern(
                                                    "d 'de' MMMM, EEEE",
                                                    summaryLocale,
                                                )
                                            ),
                                            fontSize = 11.sp,
                                            color = tataMutedColor(),
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${summary.today.confirmed} / ${summary.today.total}",
                                            fontSize = 22.sp,
                                            lineHeight = 27.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tataTextColor(),
                                        )
                                        Text(
                                            "dosis completadas",
                                            fontSize = 10.sp,
                                            color = tataMutedColor(),
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = {
                                        ((summary.today.percentage ?: 0.0) / 100).toFloat()
                                    },
                                    color = Color(0xFF75A985),
                                    trackColor =
                                        if (darkTheme) MaterialTheme.colorScheme.surfaceVariant
                                        else Color(0xFFE4E5EC),
                                    modifier =
                                        Modifier.padding(top = 12.dp).fillMaxWidth().height(6.dp),
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            SummaryCard(listOf(TataLavender, Color(0xFFE7DFFC)), onAgenda) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Próxima dosis", color = tataMutedColor(), fontSize = 11.sp)
                                        Text(
                                            summary.nextDose?.medicationName
                                                ?: "No hay próximas tomas",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = tataTextColor(),
                                        )
                                        summary.nextDose?.let {
                                            Text(it.dose, fontSize = 11.sp, color = tataTextColor())
                                        }
                                    }
                                    summary.nextDose?.let {
                                        Text(
                                            it.scheduledAt
                                                .atZone(ZoneId.systemDefault())
                                                .format(
                                                    DateTimeFormatter.ofPattern(
                                                        "h:mm a",
                                                        summaryLocale,
                                                    )
                                                ),
                                            fontSize = 18.sp,
                                            lineHeight = 23.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tataTextColor(),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(18.dp))
                            SummaryCard(listOf(TataCream, Color(0xFFFBE9C8))) {
                                Text("Atención", fontSize = 11.sp, color = tataAccentColor(Color(0xFFC48C29)))
                                Text(
                                    summary.stockAttention.firstOrNull()?.let {
                                        "Quedan ${it.remaining} unidades de ${it.medicationName}."
                                    }
                                        ?: if (summary.inventoryAvailable)
                                            "No hay alertas de reposición."
                                        else "No pudimos consultar el inventario.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tataTextColor(),
                                )
                                Text(
                                    if (summary.stockAttention.isEmpty())
                                        "Revisa los medicamentos y sus cantidades."
                                    else "Considera solicitar tu reabastecimiento.",
                                    fontSize = 11.sp,
                                    color = tataMutedColor(),
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Monitoreo rápido",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tataTextColor(),
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                QuickAction(
                                    "Ver agenda",
                                    R.raw.family_agenda,
                                    TataMint,
                                    onAgenda,
                                    Modifier.weight(1f),
                                )
                                QuickAction(
                                    "Historial",
                                    R.raw.family_history,
                                    TataLavender,
                                    onHistory,
                                    Modifier.weight(1f),
                                )
                                QuickAction(
                                    "Contactar",
                                    R.raw.family_contact,
                                    Color(0xFFECF5FB),
                                    onContact,
                                    Modifier.weight(1f),
                                )
                            }
                            TextButton(
                                onClick = onAddMedication,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Text("Agregar medicamento", color = tataAccentColor(TataDeepNavy))
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }
            CaregiverTabBar(
                CaregiverTab.Home,
                { tab ->
                    when (tab) {
                        CaregiverTab.Home -> onRetry()
                        CaregiverTab.Alerts -> onAlerts()
                        CaregiverTab.Notes -> onNotes()
                        CaregiverTab.Person -> onChangePerson()
                        CaregiverTab.More -> more = true
                    }
                },
            )
        }
    }
    if (more)
        AlertDialog(
            onDismissRequest = { more = false },
            title = { Text("Más opciones") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            more = false
                            onTreatments()
                        }
                    ) {
                        Text("Tratamientos")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onMedications()
                        }
                    ) {
                        Text("Medicamentos")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onAddMedication()
                        }
                    ) {
                        Text("Agregar medicamento")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onAccessibility()
                        }
                    ) {
                        Text("Accesibilidad")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onNotificationPreferences()
                        }
                    ) {
                        Text("Preferencias de notificación")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onSubscription()
                        }
                    ) {
                        Text("Plan y suscripción")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onChangePerson()
                        }
                    ) {
                        Text("Vincular otra persona")
                    }
                    TextButton(
                        onClick = {
                            more = false
                            onRetry()
                        }
                    ) {
                        Text("Actualizar resumen")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { more = false }) { Text("Cerrar") } },
        )
}
