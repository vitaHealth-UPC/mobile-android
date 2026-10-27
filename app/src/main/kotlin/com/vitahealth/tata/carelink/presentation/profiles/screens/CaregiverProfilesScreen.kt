package com.vitahealth.tata.carelink.presentation.profiles.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.carelink.presentation.profiles.viewmodel.CaregiverProfilesState
import com.vitahealth.tata.carelink.presentation.profiles.viewmodel.CaregiverProfilesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfilesScreen(
    state: CaregiverProfilesState,
    onForm: () -> Unit,
    onName: (String) -> Unit,
    onDate: (LocalDate) -> Unit,
    onContact: (String) -> Unit,
    onRelationship: (String) -> Unit,
    onPhone: (String) -> Unit,
    onSave: () -> Unit,
    onRefresh: () -> Unit,
    onOpenAdult: (LinkedAdult) -> Unit,
    onAdultConsent: (ProfileLinkingCode) -> Unit,
    onSignOut: () -> Unit,
    onLinkSocial: (() -> Unit)? = null,
) {
    var dateDialog by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize()
            .background(tataBackgroundColor())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Personas vinculadas",
            fontFamily = FontFamily(Font(R.font.tata_serif)),
            fontSize = 29.sp,
            color = tataTextColor(),
        )
        Text(
            "Acompaña sus tratamientos y confirma el vínculo con su consentimiento.",
            fontSize = 12.sp,
            color = tataMutedColor(),
        )
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.requiresAccountRecovery) {
            Text(
                if (state.error == "RESOURCE_ACCESS_DENIED")
                    "Esta cuenta no puede consultar estos perfiles."
                else "La sesión ya no está disponible. Vuelve a iniciar sesión.",
                color = TataError,
            )
            TataButton("Volver al acceso", onSignOut)
            return@Column
        }
        state.error?.let {
            Text(
                when (it) {
                    "CONTACT_INCOMPLETE" -> "Completa los tres datos del contacto de emergencia."
                    "VALIDATION_ERROR" -> "Completa el nombre y una fecha de nacimiento válida."
                    "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
                    else -> "No pudimos completar la operación. Inténtalo nuevamente."
                },
                color = TataError,
            )
        }
        state.adults.forEach { adult ->
            TataCard(containerColor = tataTonalColor(TataMint)) {
                Text(
                    adult.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tataTextColor(),
                )
                Text("Vínculo activo", fontSize = 12.sp, color = tataMutedColor())
                TataButton("Ver resumen", { onOpenAdult(adult) }, enabled = !state.busy)
            }
        }
        if (state.adults.isEmpty() && !state.showForm && state.code == null)
            TataCard { Text("Aún no tienes personas vinculadas.", color = tataMutedColor()) }
        state.code?.let { link ->
            TataCard(containerColor = tataTonalColor(TataLavender)) {
                Text(
                    link.olderAdultName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tataTextColor(),
                )
                Text("Código temporal", color = tataMutedColor())
                Text(link.code, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = tataAccentColor())
                Text(
                    "Comparte este código con el adulto mayor para que revise y acepte el consentimiento. Vence en 15 minutos.",
                    fontSize = 12.sp,
                    color = tataMutedColor(),
                )
                TataButton(
                    "Continuar con el adulto mayor",
                    { onAdultConsent(link) },
                    enabled = !state.busy,
                )
                TataButton(
                    "Renovar código",
                    onSave,
                    style = TataButtonStyle.Secondary,
                    enabled = !state.busy,
                )
            }
        }
        if (state.showForm) {
            TataFormField(
                "Nombre completo",
                state.name,
                onName,
                enabled = !state.busy && state.createdAdult == null,
            )
            TextButton(
                onClick = { dateDialog = true },
                enabled = !state.busy && state.createdAdult == null,
            ) {
                Text(state.birthDate?.toString() ?: "Seleccionar fecha de nacimiento")
            }
            TataFormField(
                "Contacto de emergencia (opcional)",
                state.contactName,
                onContact,
                enabled = !state.busy && state.createdAdult == null,
            )
            TataFormField(
                "Parentesco",
                state.relationship,
                onRelationship,
                enabled = !state.busy && state.createdAdult == null,
            )
            TataFormField(
                "Teléfono",
                state.phone,
                onPhone,
                enabled = !state.busy && state.createdAdult == null,
            )
            TataButton(
                if (state.createdAdult == null) "Guardar y generar vínculo" else "Generar código",
                onSave,
                enabled = !state.busy,
            )
        } else if (state.code == null)
            TataButton("Registrar adulto mayor", onForm, enabled = !state.busy)
        TextButton(onClick = onRefresh, enabled = !state.busy) { Text("Actualizar vínculos") }
        onLinkSocial?.let { action ->
            TextButton(onClick = action, enabled = !state.busy) {
                Text("Vincular Google o Facebook")
            }
        }
        TextButton(onClick = onSignOut, enabled = !state.busy) { Text("Cerrar sesión") }
    }
    if (dateDialog) {
        val picker =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    state.birthDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
            )
        DatePickerDialog(
            onDismissRequest = { dateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker.selectedDateMillis?.let {
                            onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        dateDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = { TextButton(onClick = { dateDialog = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = picker)
        }
    }
}
