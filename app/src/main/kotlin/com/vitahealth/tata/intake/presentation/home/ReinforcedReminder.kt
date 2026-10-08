package com.vitahealth.tata.intake.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.Instant

/**
 * The backend sends the reinforced reminder once a scheduled intake is still unconfirmed. The intake API
 * does not say whether that push went out, so the home shows the reminder while the next dose is still
 * pending after its scheduled time; confirming it or the backend marking it omitted removes it.
 */
internal fun isReinforcedReminderDue(dose: NextDoseReadModel, now: Instant): Boolean =
    dose.status == DoseStatus.PENDING && !now.isBefore(dose.scheduledAt)

/** "Segundo recordatorio" card of Figma 563:3682; warning text on its surface keeps 5.4:1 contrast. */
@Composable
internal fun ReinforcedReminderCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TataWarningSurface, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = stringResource(R.string.intake_reinforced_reminder_title),
            color = TataWarning,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.intake_reinforced_reminder_text),
            color = tataTextColor(),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(name = "Segundo recordatorio", showBackground = true, widthDp = 393)
@Composable
private fun ReinforcedReminderCardPreview() {
    TataTheme { ReinforcedReminderCard(Modifier.padding(16.dp)) }
}
