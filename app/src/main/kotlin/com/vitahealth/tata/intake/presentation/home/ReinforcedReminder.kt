package com.vitahealth.tata.intake.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.theme.TataTheme
import java.time.Instant

/**
 * The backend sends the reinforced reminder once a scheduled intake is still unconfirmed. The intake API
 * does not say whether that push went out, so the home shows the reminder while the next dose is still
 * pending after its scheduled time; confirming it or the backend marking it omitted removes it.
 */
internal fun isReinforcedReminderDue(dose: NextDoseReadModel, now: Instant): Boolean =
    dose.status == DoseStatus.PENDING && !now.isBefore(dose.scheduledAt)

/** Native reminder notice of Figma 563:3682. Its state comes from the pending scheduled dose. */
@Composable
internal fun ReinforcedReminderCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .background(Color(0xFFFFF5E0), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = stringResource(R.string.intake_reinforced_reminder_title),
            color = Color(0xFF855E1F),
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.intake_reinforced_reminder_text),
            color = Color(0xFF78859E),
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            fontSize = 9.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(name = "Segundo recordatorio", showBackground = true, widthDp = 393)
@Composable
private fun ReinforcedReminderCardPreview() {
    TataTheme { ReinforcedReminderCard(Modifier.padding(16.dp)) }
}
