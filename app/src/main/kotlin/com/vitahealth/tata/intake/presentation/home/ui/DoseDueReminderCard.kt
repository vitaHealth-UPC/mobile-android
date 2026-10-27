package com.vitahealth.tata.intake.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R

/** Figma #39 due-dose snackbar; does not imply a follow-up push has been delivered. */
@Composable
internal fun DoseDueReminderCard(
    medicationName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .background(Color(0xFFFFF5E0), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = stringResource(R.string.intake_due_reminder_title),
            color = Color(0xFF855E1F),
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.intake_due_reminder_body, medicationName),
            color = Color(0xFF78859E),
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            fontSize = 9.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
