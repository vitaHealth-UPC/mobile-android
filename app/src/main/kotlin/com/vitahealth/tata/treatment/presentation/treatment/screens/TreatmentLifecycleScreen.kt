package com.vitahealth.tata.treatment.presentation.treatment.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.ui.ReviewTreatment
import com.vitahealth.tata.treatment.presentation.treatment.ui.TreatmentAdherenceCard
import com.vitahealth.tata.treatment.presentation.treatment.ui.TreatmentStatusNotice
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState

@Composable
fun TreatmentLifecycleScreen(
    state: TreatmentLifecycleUiState,
    onActivate: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
    onEditRegimen: (() -> Unit)? = null,
    onRetryManagement: (() -> Unit)? = null,
    onManageMedication: (() -> Unit)? = null,
    onCompleteConfiguration: (() -> Unit)? = null,
    onReactivateMedication: (() -> Unit)? = null,
    navigationBar: @Composable () -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(tataBackgroundColor())) {
        Column(
            modifier =
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            if (state.isRefreshingManagement) {
                Text("Cargando tratamiento...", color = tataTextColor())
            } else if (state.managementReadFailed) {
                Text(
                    "Tratamiento",
                    color = tataTextColor(),
                    style = MaterialTheme.typography.headlineMedium,
                )
                onRetryManagement?.let { retry -> TataButton("Reintentar", retry) }
                TataButton("Volver al detalle", onOpenDetail)
            } else if (state.status == TreatmentStatus.DRAFT) {
                ReviewTreatment(
                    state = state,
                    onActivate = onActivate,
                    onCompleteConfiguration = onCompleteConfiguration,
                )
            } else {
                ManageTreatment(
                    state = state,
                    onPause = onPause,
                    onResume = onResume,
                    onOpenDetail = onOpenDetail,
                    onEditRegimen = onEditRegimen,
                    onManageMedication = onManageMedication,
                    onRetryAdherence = onRetryManagement,
                    onReactivateMedication = onReactivateMedication,
                )
            }

            state.errorMessage
                ?.takeUnless { state.configurationIncomplete }
                ?.let { message ->
                    TataCard(
                        containerColor = tataTonalColor(TataErrorSurface),
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    ) {
                        Text(text = message, color = TataError, fontWeight = FontWeight.SemiBold)
                    }
                }

            Spacer(Modifier.height(32.dp))
        }
        if (
            !state.managementReadFailed &&
                !state.isRefreshingManagement &&
                (state.regimenSaved || state.status == TreatmentStatus.PAUSED)
        ) {
            TreatmentStatusNotice(
                state.regimenSaved,
                medicationInactive = state.medicationInactive,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
            )
        }
        if (state.status != TreatmentStatus.DRAFT) navigationBar()
    }
}

@Composable
private fun ManageTreatment(
    state: TreatmentLifecycleUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpenDetail: () -> Unit,
    onEditRegimen: (() -> Unit)?,
    onManageMedication: (() -> Unit)?,
    onRetryAdherence: (() -> Unit)?,
    onReactivateMedication: (() -> Unit)?,
) {
    Text(
        text = "Tratamiento de " + firstName(state.olderAdultName),
        fontFamily = FontFamily(Font(R.font.tata_serif)),
        fontSize = 29.sp,
        lineHeight = 35.sp,
        color = tataTextColor(),
    )
    Text(
        text = "Gestiona la pauta sin perder historial.",
        style = MaterialTheme.typography.bodySmall,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 4.dp),
    )

    TataCard(Modifier.fillMaxWidth().padding(top = 18.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(36.dp).background(tataTonalColor(TataLavender), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val initials =
                    state.olderAdultName
                        .trim()
                        .split(" ")
                        .filter(String::isNotBlank)
                        .take(2)
                        .mapNotNull { it.firstOrNull() }
                        .joinToString("")
                Text(initials, color = tataAccentColor(), fontSize = 13.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    state.olderAdultName,
                    color = tataTextColor(),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                state.activeMedicationCount?.let { count ->
                    Text(
                        if (count == 1) "1 medicamento activo" else "$count medicamentos activos",
                        color = tataMutedColor(),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
            state.adherence?.let { evidence ->
                Text(
                    "${evidence.percent}% adherencia",
                    color = tataAccentColor(Color(0xFF2E734D)),
                    fontSize = 10.sp,
                    modifier =
                        Modifier.background(tataTonalColor(Color(0xFFE8F5EB)), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }

    TataCard(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).clickable(onClick = onOpenDetail)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                state.treatmentName,
                Modifier.weight(1f),
                color = tataTextColor(),
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (state.medicationInactive) "Inactivo"
                else if (state.status == TreatmentStatus.ACTIVE) "Activo" else "Pausado",
                color =
                    if (state.status == TreatmentStatus.ACTIVE) Color(0xFF2E734D)
                    else Color(0xFFAF7929),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            state.medicationLabel,
            color = tataTextColor(),
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 14.dp),
        )
        Text(
            "${state.dosage}, ${state.frequency}, ${state.scheduleText.replace(",", " · ")}",
            color = tataMutedColor(),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            listOf(
                    state.instructions.takeIf(String::isNotBlank),
                    if (state.usesDefaultFollowUp) "Recordatorio del sistema"
                    else "Recordatorio +${state.reminderDelayMinutes} min",
                )
                .filterNotNull()
                .joinToString(", "),
            color = tataMutedColor(),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onEditRegimen ?: onOpenDetail,
                enabled = !state.isLoading && !state.medicationInactive,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = tataTonalColor(TataLavender),
                        contentColor = tataAccentColor(),
                    ),
                border = BorderStroke(1.dp, tataOutlineColor(Color(0xFFC2B5E5))),
            ) {
                Text(if (onEditRegimen != null) "Editar pauta" else "Ver detalle", fontSize = 14.sp)
            }
            OutlinedButton(
                onClick =
                    if (state.medicationInactive) onReactivateMedication ?: {}
                    else if (state.status == TreatmentStatus.ACTIVE) onPause else onResume,
                enabled =
                    !state.isLoading &&
                        (!state.medicationInactive || onReactivateMedication != null),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = tataTonalColor(TataLavender),
                        contentColor = tataAccentColor(),
                    ),
                border = BorderStroke(1.dp, tataOutlineColor(Color(0xFFC2B5E5))),
            ) {
                Text(
                    when {
                        state.medicationInactive -> "Reactivar"
                        state.isLoading -> "Actualizando..."
                        state.status == TreatmentStatus.ACTIVE -> "Pausar"
                        else -> "Reanudar"
                    },
                    fontSize = 14.sp,
                )
            }
        }
    }

    TataCard(
        containerColor = tataTonalColor(Color(0xFFFFF3E2)),
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    ) {
        Text(
            text = "Historial protegido",
            color = tataTextColor(),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Pausar o editar solo afecta las tomas futuras.",
            color = tataMutedColor(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 5.dp),
        )
    }

    // Medication deactivation is handled by the existing medication-management
    // workflow. The destination reloads authorization and requests confirmation;
    // this screen must never issue a second, unguarded mutation.
    onManageMedication?.let { manage ->
        TextButton(
            onClick = manage,
            enabled = !state.isLoading && !state.medicationInactive,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                if (state.medicationInactive) "Medicamento desactivado"
                else "Desactivar medicamento",
                fontSize = 12.sp,
                color =
                    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.error
                    else TataError,
            )
        }
    }

    // A per-treatment metric must not borrow an older-adult aggregate percentage.
    // Replace this informative state only after a treatment-scoped query is wired.
    TreatmentAdherenceCard(
        state.adherence,
        state.adherenceError,
        onRetryAdherence,
        Modifier.padding(top = 14.dp),
    )
}

private fun firstName(fullName: String): String =
    fullName.trim().substringBefore(" ").ifBlank { "la persona" }
