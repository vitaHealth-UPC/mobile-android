package com.vitahealth.tata.monitoring.presentation.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataInterFontFamily
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.LocalDate
import java.time.Period

/**
 * Identifies the linked adult from the authorized profile endpoint.
 *
 * Figma's photograph is deliberately not embedded as a substitute for a real profile image. An
 * unknown birth date does not become a guessed age.
 */
@Composable
internal fun NotesPersonCard(person: MonitoredPerson) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = tataCardColor(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tataOutlineColor()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(Color(0xFFF1ECFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        person.fullName.trim().split(Regex("\\s+")).take(2).joinToString("") {
                            it.take(1).uppercase()
                        },
                    fontFamily = TataInterFontFamily,
                    color = TataNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.fullName,
                    color = tataTextColor(),
                    fontFamily = TataInterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                val caption =
                    person.birthDate?.let {
                        stringResource(
                            R.string.notes_person_age_active,
                            Period.between(it, LocalDate.now()).years,
                        )
                    } ?: stringResource(R.string.notes_person_link_active)
                Text(
                    text = caption,
                    color = tataMutedColor(),
                    fontSize = 11.sp,
                    fontFamily = TataInterFontFamily,
                )
            }
            Text(
                text = stringResource(R.string.notes_person_active),
                color =
                    if (LocalTataAccessibility.current.darkTheme) Color(0xFFB8E6C7)
                    else TataSuccess,
                fontFamily = TataInterFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier =
                    Modifier.background(tataTonalColor(TataMint), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
