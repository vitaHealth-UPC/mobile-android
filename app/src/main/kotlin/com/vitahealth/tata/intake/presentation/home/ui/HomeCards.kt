package com.vitahealth.tata.intake.presentation.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.presentation.home.mappers.relativeTimeLabel
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val homeSerif = FontFamily(Font(R.font.tata_serif))

internal val homeInter = FontFamily(Font(R.font.tata_inter))

@Composable
internal fun NextDoseCard(dose: NextDoseReadModel, onOpenDoseDetail: (String) -> Unit) {
    val locale = Locale.forLanguageTag("es-PE")
    Row(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFEEE8FF), Color(0xFFE5DDF8))))
            .clickable(role = Role.Button) { onOpenDoseDetail(dose.id) }
            .padding(start = 18.dp, end = 14.dp, top = 18.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_next_dose),
                fontFamily = homeInter,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataNavy,
            )
            Text(
                dose.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("h:mm a", locale))
                    .lowercase()
                    .replace("am", "a. m.")
                    .replace("pm", "p. m."),
                fontFamily = homeInter,
                fontSize = 31.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                dose.medicationName,
                fontFamily = homeInter,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                dose.dose,
                fontFamily = homeInter,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = TataMuted,
                modifier = Modifier.padding(top = 3.dp),
            )
            relativeTimeLabel(dose)?.let { remaining ->
                val label =
                    stringResource(
                        if (remaining.hours) R.string.home_in_hours else R.string.home_in_minutes,
                        remaining.value,
                    )
                Text(
                    label,
                    fontFamily = homeInter,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5961B2),
                    modifier =
                        Modifier.padding(top = 4.dp)
                            .background(Color.White, CircleShape)
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                )
            }
        }
        Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.home_tablet),
                null,
                Modifier.requiredSize(176.dp).offset(y = 12.dp),
            )
        }
    }
}

@Composable
internal fun ProgressCard(progress: DailyDoseProgress?, onOpenAgenda: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(Color.White, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onOpenAgenda)
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp)
    ) {
        Text(
            stringResource(R.string.home_progress_title),
            fontFamily = homeInter,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = {
                        if (progress != null && progress.total > 0)
                            progress.completed.toFloat() / progress.total
                        else 0f
                    },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF7D6BE5),
                    trackColor = TataLavender,
                    strokeWidth = 5.dp,
                )
                Text(
                    progress?.let { "${it.completed}/${it.total}" } ?: "—",
                    fontFamily = homeInter,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TataNavy,
                )
            }
            Column(Modifier.weight(1f).padding(start = 20.dp)) {
                Text(
                    stringResource(R.string.home_completed),
                    fontFamily = homeInter,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    color = TataMuted,
                )
                Text(
                    when {
                        progress == null -> stringResource(R.string.home_progress_unavailable)
                        progress.total == 0 -> stringResource(R.string.home_progress_empty)
                        progress.completed == progress.total ->
                            stringResource(R.string.home_progress_done)
                        progress.completed > 0 ->
                            stringResource(R.string.home_progress_in_progress)
                        else -> stringResource(R.string.home_progress_encouragement)
                    },
                    fontFamily = homeInter,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TataNavy,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text("›", fontSize = 22.sp, color = TataNavy)
        }
    }
}

@Composable
internal fun HomeShortcut(
    title: String,
    subtitle: String,
    color: Color,
    icon: Int,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    Column(
        modifier
            .shadow(5.dp, RoundedCornerShape(18.dp))
            .background(color, RoundedCornerShape(18.dp))
            .clickable(
                enabled = onClick != null,
                role = Role.Button,
                onClick = { onClick?.invoke() },
            )
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                fontFamily = homeInter,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TataText,
                modifier = Modifier.weight(1f),
            )
            TataSvgIcon(icon, Modifier.size(if (icon == R.raw.home_list) 27.dp else 28.dp))
        }
        Text(
            subtitle,
            fontFamily = homeInter,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
