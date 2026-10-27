package com.vitahealth.tata.monitoring.presentation.notes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsSecondaryText
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor

@Composable
internal fun EmptyNotesCard() {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = tataTonalColor(TataMint)) {
        Text(
            text = stringResource(R.string.notes_empty_title),
            color = tataTextColor(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.notes_empty_message),
            color = AlertsSecondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun NoteCard(
    note: FollowUpNote,
    mine: Boolean,
    recordedAt: String,
    highlighted: Boolean = false,
) {
    Column(
        Modifier.fillMaxWidth()
            .shadow(
                6.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .background(if (mine) tataTonalColor(TataLavender) else tataCardColor(), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (highlighted)
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    TataSvgIcon(R.raw.notes_recent_marker, Modifier.size(36.dp))
                    Text("•", color = Color(0xFF1A336B), fontSize = 20.sp)
                }
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.text,
                    color = tataTextColor(),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                )
                Text(
                    text =
                        stringResource(
                            R.string.notes_meta,
                            stringResource(
                                if (mine) R.string.notes_author_you else R.string.notes_author_other
                            ),
                            recordedAt,
                        ),
                    color = AlertsSecondaryText,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
