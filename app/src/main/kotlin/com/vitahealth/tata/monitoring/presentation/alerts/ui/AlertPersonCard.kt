package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.LocalDate
import java.time.Period

@Composable
internal fun AlertPersonCard(person: MonitoredPerson, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = tataCardColor(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
    ) {
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(44.dp).background(tataTonalColor(Color(0xFFF1ECFF)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    person.fullName.trim().split(Regex("\\s+")).take(2).joinToString("") {
                        it.take(1).uppercase()
                    },
                    color = tataAccentColor(TataNavy),
                    fontSize = 15.sp,
                )
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    person.fullName,
                    color = tataTextColor(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                )
                person.birthDate?.let {
                    Text(
                        stringResource(
                            R.string.alert_person_age,
                            Period.between(it, LocalDate.now()).years,
                        ),
                        color = AlertsSecondaryText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            TextButton(onClick = onOpen) {
                Text(stringResource(R.string.alert_person_summary), fontSize = 12.sp)
            }
        }
    }
}
