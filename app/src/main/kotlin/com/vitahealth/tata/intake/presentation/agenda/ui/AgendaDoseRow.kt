package com.vitahealth.tata.intake.presentation.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val agendaLocale = Locale.forLanguageTag("es-PE")

@Composable
internal fun AgendaDoseRow(
    dose: DoseDetailReadModel,
    state: IntakeAgendaUiState,
    onOpenDose: (String) -> Unit,
) {
    val localTime = dose.scheduledAt.atZone(state.week.zone)
    val label =
        when (dose.status) {
            DoseStatus.PENDING -> "Pendiente"
            DoseStatus.CONFIRMED -> "Confirmada"
            DoseStatus.LATE -> "Tardía"
            DoseStatus.OMITTED -> "Omitida"
        }
    val colors =
        when {
            dose.status == DoseStatus.OMITTED -> listOf(TataErrorSurface, Color(0xFFFADFE1))
            dose.status == DoseStatus.LATE || localTime.hour in 12..17 ->
                listOf(Color(0xFFFFF5E3), Color(0xFFFBE9C7))
            localTime.hour >= 21 -> listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))
            else -> listOf(Color(0xFFF1ECFF), Color(0xFFE7DFFC))
        }
    Row(
        Modifier.fillMaxWidth()
            .drawBehind {
                drawLine(
                    Color(0xFFE6E3F1),
                    Offset(49.dp.toPx(), 0f),
                    Offset(49.dp.toPx(), size.height),
                    2.dp.toPx(),
                )
            }
            .padding(
                bottom = if (dose.id == state.selectedDoses.lastOrNull()?.id) 12.dp else 28.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(68.dp), horizontalAlignment = Alignment.End) {
            Text(
                localTime
                    .format(DateTimeFormatter.ofPattern("h:mm a", agendaLocale))
                    .lowercase(agendaLocale),
                fontSize = 10.sp,
                color = TataMuted,
            )
            TataSvgIcon(
                if (localTime.hour < 18) R.raw.agenda_sun else R.raw.agenda_moon,
                Modifier.padding(top = 6.dp, end = 8.dp).size(22.dp).background(TataSurface),
            )
        }
        Row(
            Modifier.weight(1f)
                .shadow(
                    6.dp,
                    RoundedCornerShape(16.dp),
                    ambientColor = Color(0x141A2138),
                    spotColor = Color(0x141A2138),
                )
                .background(Brush.horizontalGradient(colors), RoundedCornerShape(16.dp))
                .clickable(role = Role.Button) { onOpenDose(dose.id) }
                .semantics { contentDescription = "${dose.medicationName}, $label" }
                .heightIn(min = 88.dp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    dose.medicationName,
                    color = TataText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
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
                if (dose.status == DoseStatus.CONFIRMED || dose.status == DoseStatus.LATE)
                    Text("✓", color = TataSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (dose.status == DoseStatus.OMITTED)
                    Text("×", color = TataError, fontSize = 14.sp)
            }
        }
    }
}
