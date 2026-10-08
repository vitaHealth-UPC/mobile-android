package com.vitahealth.tata.intake.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.intake.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataHighContrastMuted
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataSuccess
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val resultSerif = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif))

/**
 * US-23 result screens: "Late Dose Confirmed" (Figma 563:3770) and "Omission Preserved" (563:3844).
 * The prototype cards "Tu familia ha sido notificada" and "Próxima toma" are left out: no API tells the app
 * that the family was notified, and the next dose is already on the home this screen returns to.
 */
@Composable
internal fun ConfirmationResult(
    outcome: ConfirmationOutcome,
    dose: DoseDetailReadModel,
    onBack: () -> Unit,
) {
    val late = outcome == ConfirmationOutcome.LATE
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(96.dp)
                .background(if (late) TataMint else TataWarningSurface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (late) "✓" else "!",
                color = if (late) TataSuccess else TataWarning,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = stringResource(if (late) R.string.intake_result_late_title else R.string.intake_result_omitted_title),
            fontFamily = resultSerif,
            fontSize = 32.sp,
            color = tataTextColor(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp).semantics { heading() },
        )
        Text(
            text = stringResource(if (late) R.string.intake_result_late_subtitle else R.string.intake_result_omitted_subtitle),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = tataTextColor(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        val shownAt = if (late) dose.confirmedAt else dose.scheduledAt
        TimeCard(
            label = stringResource(if (late) R.string.intake_result_confirmed_at else R.string.intake_result_scheduled_at),
            medication = dose.medicationName,
            at = shownAt,
            modifier = Modifier.padding(top = 28.dp),
        )
        if (late) {
            TataButton(stringResource(R.string.intake_result_back_home), onBack, Modifier.padding(top = 28.dp))
        } else {
            UnavailableConfirmation(Modifier.padding(top = 28.dp))
            TataButton(stringResource(R.string.intake_result_back_home), onBack, Modifier.padding(top = 12.dp))
        }
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .background(TataWarningSurface, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(if (late) R.string.intake_result_late_note_title else R.string.intake_result_omitted_note_title),
                color = TataWarning,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(if (late) R.string.intake_result_late_note else R.string.intake_result_omitted_note),
                color = tataTextColor(),
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun TimeCard(label: String, medication: String, at: Instant?, modifier: Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val day = at?.atZone(zone)?.toLocalDate()
    val dayText = if (day == null) {
        stringResource(R.string.intake_result_time_unknown)
    } else if (day == LocalDate.now(zone)) {
        stringResource(R.string.intake_result_today, day.format(DateTimeFormatter.ofPattern("d MMMM", locale)))
    } else {
        day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = "$label · $medication", color = TataHighContrastMuted, fontSize = 14.sp)
        Text(text = dayText, color = TataHighContrastMuted, fontSize = 14.sp)
        Text(
            text = at?.atZone(zone)?.toLocalTime()?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)) ?: "—",
            color = tataTextColor(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Grey "Confirmación no disponible" of the Figma: a state, not an action. */
@Composable
private fun UnavailableConfirmation(modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TataBorder, RoundedCornerShape(28.dp))
            .semantics { disabled() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.intake_result_confirmation_unavailable), color = TataHighContrastMuted, fontWeight = FontWeight.SemiBold)
    }
}

private val previewDose = DoseDetailReadModel(
    id = "101", treatmentId = "t", medicationId = "m", olderAdultId = "adult-1",
    medicationName = "Losartán 50 mg", dose = "1 tableta", instructions = "",
    scheduledAt = Instant.parse("2026-10-05T13:00:00Z"), status = DoseStatus.LATE,
    confirmedAt = Instant.parse("2026-10-05T13:02:00Z"),
)

@Preview(name = "Toma confirmada con retraso", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LateResultPreview() {
    TataTheme { ConfirmationResult(ConfirmationOutcome.LATE, previewDose, onBack = {}) }
}

@Preview(name = "Omisión conservada", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OmittedResultPreview() {
    TataTheme { ConfirmationResult(ConfirmationOutcome.OMISSION_PRESERVED, previewDose.copy(status = DoseStatus.OMITTED, confirmedAt = null), onBack = {}) }
}
