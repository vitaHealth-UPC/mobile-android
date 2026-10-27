package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState

/** Native review of the actual draft; agenda markers represent configured times, not adherence. */
@Composable
internal fun ReviewTreatment(
    state: TreatmentLifecycleUiState,
    onActivate: () -> Unit,
    onCompleteConfiguration: (() -> Unit)? = null,
) {
    Text(
        "Crear tratamiento",
        fontFamily = FontFamily(Font(R.font.tata_serif)),
        fontSize = 29.sp,
        lineHeight = 35.sp,
        color = tataTextColor(),
    )
    Text(
        "Cuidador, " + state.olderAdultName,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 4.dp),
    )
    ReviewStepper(Modifier.padding(top = 12.dp), if (state.configurationIncomplete) 0 else 3)
    ReviewLabel("Nombre del tratamiento", Modifier.padding(top = 10.dp))
    ReviewField(state.treatmentName)
    ReviewLabel("Medicamento", Modifier.padding(top = 2.dp))
    Row(
        Modifier.fillMaxWidth()
            .padding(top = 8.dp)
            .shadow(
                2.dp,
                RoundedCornerShape(15.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .background(tataCardColor(), RoundedCornerShape(15.dp))
            .heightIn(min = 58.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            state.medicationLabel,
            Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = tataTextColor(),
        )
        TataSvgIcon(
            R.raw.treatment_review_medication,
            Modifier.size(24.dp),
            colorFilter =
                if (LocalTataAccessibility.current.darkTheme) {
                    ColorFilter.tint(tataAccentColor())
                } else {
                    null
                },
        )
    }
    TataCard(
        Modifier.fillMaxWidth()
            .padding(top = 10.dp)
            .shadow(
                2.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReviewValue("Dosis", state.dosage, Modifier.weight(1f))
            ReviewValue("Frecuencia", state.frequency, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReviewValue("Horario", state.scheduleText.replace(",", " · "), Modifier.weight(1f))
            ReviewValue(
                "Indicaciones",
                state.instructions.ifBlank { "Sin indicaciones adicionales" },
                Modifier.weight(1f),
            )
        }
    }
    TataCard(
        Modifier.fillMaxWidth().padding(top = 14.dp).heightIn(min = 90.dp),
        containerColor = tataTonalColor(TataLavender),
    ) {
        Text(
            "Recordatorios activos",
            color = tataTextColor(),
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Avisar ${state.reminderDelayMinutes} min después si no confirma.",
            color = tataMutedColor(),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
    TataButton(
        text =
            if (state.isLoading) "Activando..."
            else if (state.configurationIncomplete) "Completar configuración"
            else "Activar tratamiento",
        enabled =
            !state.isLoading && (!state.configurationIncomplete || onCompleteConfiguration != null),
        onClick = if (state.configurationIncomplete) onCompleteConfiguration ?: {} else onActivate,
        modifier = Modifier.padding(top = 24.dp),
    )
    val times = state.scheduleText.split(",").map(String::trim).filter(String::isNotBlank)
    if (state.configurationIncomplete) {
        TataCard(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            containerColor = tataTonalColor(Color(0xFFFFF5DB)),
        ) {
            Text(
                "Configuración incompleta",
                color = tataTextColor(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Completa dosis, frecuencia, horario y recordatorios antes de activar.",
                color = tataMutedColor(),
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    } else if (times.isNotEmpty())
        Column(
            Modifier.fillMaxWidth()
                .padding(top = 16.dp)
                .background(
                    if (LocalTataAccessibility.current.darkTheme) {
                        Brush.horizontalGradient(listOf(tataCardColor(), tataCardColor()))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9)))
                    },
                    RoundedCornerShape(18.dp),
                )
                .padding(16.dp)
        ) {
            Text(
                "Vista previa de agenda",
                color = tataTextColor(),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            times.chunked(3).forEach { group ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    group.forEach { time ->
                        Column(Modifier.weight(1f)) {
                            Box(
                                Modifier.fillMaxWidth()
                                    .height(6.dp)
                                    .background(tataAccentColor(TataNavy), RoundedCornerShape(3.dp))
                            )
                            Text(
                                time,
                                color = tataMutedColor(),
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                    }
                }
            }
        }
}

@Composable
private fun ReviewLabel(label: String, modifier: Modifier = Modifier) {
    Text(label, modifier, color = tataMutedColor(), fontSize = 11.sp, lineHeight = 14.sp)
}

@Composable
private fun ReviewField(value: String) {
    Box(
        Modifier.fillMaxWidth()
            .padding(top = 8.dp)
            .shadow(
                2.dp,
                RoundedCornerShape(15.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .background(tataCardColor(), RoundedCornerShape(15.dp))
            .heightIn(min = 58.dp)
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(value, color = tataTextColor(), fontSize = 14.sp)
    }
}

@Composable
private fun ReviewValue(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        ReviewLabel(label)
        Text(
            value,
            color = tataTextColor(),
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}

@Composable
private fun ReviewStepper(modifier: Modifier, activeStep: Int) {
    Box(
        modifier
            .fillMaxWidth()
            .background(
                if (LocalTataAccessibility.current.darkTheme) {
                    Brush.horizontalGradient(listOf(tataCardColor(), tataCardColor()))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFFF1ECFF), Color(0xFFF7EFFF)))
                },
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .padding(horizontal = 31.dp)
                .padding(top = 8.dp)
                .height(2.dp)
                .background(
                    if (LocalTataAccessibility.current.darkTheme) {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    } else {
                        Color(0xFFDBE0ED)
                    }
                )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Datos", "Pauta", "Recordatorios", "Revisar").forEachIndexed { index, label ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        if (LocalTataAccessibility.current.darkTheme) {
                            Box(
                                Modifier.size(18.dp)
                                    .background(
                                        if (index == activeStep) tataAccentColor()
                                        else tataTonalColor(TataLavender),
                                        CircleShape,
                                    )
                            )
                        } else {
                            // The exact Figma SVG remains unchanged in light mode.
                            TataSvgIcon(
                                if (index == activeStep) R.raw.treatment_step_active
                                else R.raw.treatment_step_inactive,
                                Modifier.size(18.dp),
                            )
                        }
                        Text(
                            "${index + 1}",
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            color =
                                if (LocalTataAccessibility.current.darkTheme) {
                                    if (index == activeStep) MaterialTheme.colorScheme.onPrimary
                                    else tataTextColor()
                                } else if (index == activeStep) Color.White else TataNavy,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        label,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        color = tataMutedColor(),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
