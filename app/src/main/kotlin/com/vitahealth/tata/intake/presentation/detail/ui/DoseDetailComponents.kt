package com.vitahealth.tata.intake.presentation.detail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.detail.mappers.scheduleDayLabel
import com.vitahealth.tata.intake.presentation.detail.mappers.scheduleTimeLabel
import com.vitahealth.tata.intake.presentation.detail.mappers.statusColor
import com.vitahealth.tata.intake.presentation.detail.mappers.statusMessage
import com.vitahealth.tata.intake.presentation.detail.mappers.statusTitle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText

internal val detailSerif = FontFamily(Font(R.font.tata_serif))

internal val detailInter = FontFamily(Font(R.font.tata_inter))

@Composable
internal fun DoseContent(dose: DoseDetailReadModel, nextDose: NextDoseReadModel?) {
    val scheduledDose =
        nextDose?.let {
            DoseDetailReadModel(
                it.id,
                it.treatmentId,
                it.medicationId,
                it.olderAdultId,
                it.medicationName,
                it.dose,
                it.instructions,
                it.scheduledAt,
                it.status,
            )
        } ?: dose
    Row(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFF0EBFF), Color(0xFFE7DDFB))))
            .padding(start = 3.dp, end = 16.dp, top = 9.dp, bottom = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(90.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.detail_tablet),
                null,
                Modifier.requiredSize(130.dp).offset(y = 10.dp),
            )
        }
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(
                dose.medicationName,
                fontFamily = detailInter,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
            )
            Text(
                statusTitle(dose.status),
                fontFamily = detailInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = Color(0xFF5966B8),
                modifier =
                    Modifier.padding(top = 5.dp)
                        .background(Color(0xFFF8F6FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp),
            )
            Text(
                dose.dose,
                fontFamily = detailInter,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = TataText,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    DetailSectionLabel(
        stringResource(
            if (nextDose != null || dose.status == DoseStatus.PENDING)
                R.string.detail_next_confirmation
            else R.string.detail_recorded_schedule
        )
    )
    Row(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TataSvgIcon(R.raw.detail_morning, Modifier.size(22.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                scheduleDayLabel(scheduledDose),
                fontFamily = detailInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = TataMuted,
            )
            Text(
                scheduleTimeLabel(scheduledDose),
                fontFamily = detailInter,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
            )
        }
        Text(
            statusTitle(scheduledDose.status),
            fontFamily = detailInter,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = if (scheduledDose.status == DoseStatus.PENDING) Color(0xFFC48C29) else TataNavy,
            modifier =
                Modifier.background(statusColor(scheduledDose.status), RoundedCornerShape(13.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
    DetailSectionLabel(stringResource(R.string.detail_information))
    Column(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        DetailLine(R.raw.detail_dose, stringResource(R.string.detail_dose), dose.dose)
        DetailLine(R.raw.detail_when, stringResource(R.string.detail_when), scheduleTimeLabel(dose))
        // The intake contract does not provide clinical indication or treatment frequency.
        DetailLine(
            R.raw.detail_purpose,
            stringResource(R.string.detail_purpose),
            stringResource(R.string.detail_unspecified),
        )
        DetailLine(
            R.raw.detail_instructions,
            stringResource(R.string.detail_instructions),
            dose.instructions.ifBlank { stringResource(R.string.detail_no_instructions) },
        )
    }
    Spacer(Modifier.height(19.dp))
    Box(
        Modifier.fillMaxWidth()
            .heightIn(min = 128.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFF1ECFF), Color(0xFFE5DCFA))),
                RoundedCornerShape(18.dp),
            )
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)) {
            Text(
                stringResource(R.string.detail_before_dose),
                fontFamily = detailInter,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataNavy,
            )
            Column(
                Modifier.padding(top = 12.dp, end = 72.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreparationLine("✓", stringResource(R.string.detail_prepare_medication))
                PreparationLine("✓", stringResource(R.string.detail_prepare_water))
                PreparationLine("○", stringResource(R.string.detail_prepare_seat))
            }
        }
        Image(
            painterResource(R.drawable.detail_water_plant),
            null,
            Modifier.align(Alignment.BottomEnd).padding(end = 9.dp).size(106.dp, 102.dp),
        )
    }
    Column(
        Modifier.fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                when (dose.status) {
                    DoseStatus.OMITTED -> Color(0xFFFFF5E0)
                    DoseStatus.CONFIRMED -> Color(0xFFE8F5EB)
                    else -> TataLavender
                },
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            stringResource(R.string.detail_state, statusTitle(dose.status)),
            color =
                when (dose.status) {
                    DoseStatus.OMITTED -> Color(0xFF855E1F)
                    DoseStatus.CONFIRMED -> Color(0xFF2E6E45)
                    else -> TataNavy
                },
            fontFamily = detailInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            statusMessage(dose.status),
            color = TataMuted,
            fontFamily = detailInter,
            fontSize = 9.sp,
            lineHeight = 12.sp,
        )
    }
    Spacer(Modifier.height(if (dose.status == DoseStatus.PENDING) 20.dp else 4.dp))
}

@Composable
internal fun DetailSectionLabel(label: String) {
    Text(
        label,
        fontFamily = detailInter,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = TataMuted,
        modifier = Modifier.padding(top = 23.dp, bottom = 8.dp),
    )
}

@Composable
internal fun DetailLine(icon: Int, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TataSvgIcon(icon, Modifier.size(20.dp))
        Text(
            label,
            fontFamily = detailInter,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = TataMuted,
            modifier = Modifier.padding(start = 10.dp).width(102.dp),
        )
        Text(
            value,
            fontFamily = detailInter,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TataText,
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
        )
    }
}

@Composable
internal fun PreparationLine(mark: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(mark, fontSize = 14.sp, color = Color(0xFF6E6ED1), modifier = Modifier.width(26.dp))
        Text(text, fontFamily = detailInter, fontSize = 13.sp, lineHeight = 17.sp, color = TataText)
    }
}
