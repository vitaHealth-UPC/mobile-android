package com.vitahealth.tata.monitoring.presentation.alerts.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.canMoveTo
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFeedback
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.monitoring.presentation.contact.ui.ContactAction
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

/**
 * "Acciones de seguimiento" of the alert detail: contact (US-29), attend and close (US-31) and
 * notes (US-30). Closing asks first because the backend cannot reopen an alert.
 */
@Composable
internal fun AlertFollowUpActions(
    state: AlertDetailUiState.Content,
    onUpdateStatus: (AlertStatus) -> Unit,
    modifier: Modifier = Modifier,
    noteSaved: Boolean = false,
    onAddNote: () -> Unit = {},
    contact: ContactUiState = ContactUiState.Loading,
    onRetryContact: () -> Unit = {},
) {
    var confirmClose by
        rememberSaveable(state.alert.id, state.alert.status) { mutableStateOf(false) }
    val alert = state.alert
    val busy = state.updating != null
    val firstName = ((contact as? ContactUiState.Ready)?.option?.firstName)

    TataCard(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(
                    6.dp,
                    RoundedCornerShape(18.dp),
                    ambientColor = Color(0x141A2138),
                    spotColor = Color(0x141A2138),
                )
                .background(
                    Brush.horizontalGradient(
                        if (LocalTataAccessibility.current.darkTheme) {
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                        } else {
                            listOf(Color(0xFFFFF5DF), Color(0xFFFBEAC9))
                        }
                    ),
                    RoundedCornerShape(18.dp),
                ),
        containerColor = Color.Transparent,
    ) {
        Text(
            text = stringResource(R.string.alert_actions_title),
            color = tataTextColor(),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text =
                when {
                    alert.status == AlertStatus.CLOSED -> stringResource(R.string.alert_closed_note)
                    firstName != null ->
                        stringResource(R.string.alert_actions_subtitle_named, firstName)
                    else -> stringResource(R.string.alert_actions_subtitle)
                },
            color = AlertsSecondaryText,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ContactAction(contact, onRetryContact, compact = true)
            when {
                alert.status.canMoveTo(AlertStatus.ATTENDED) ->
                    TataButton(
                        text =
                            stringResource(
                                if (state.updating == AlertStatus.ATTENDED)
                                    R.string.alert_action_saving
                                else R.string.alert_action_attend
                            ),
                        onClick = { onUpdateStatus(AlertStatus.ATTENDED) },
                        enabled = !busy,
                        modifier = Modifier.height(48.dp),
                        style = TataButtonStyle.Secondary,
                    )
                alert.status == AlertStatus.ATTENDED -> AttendedMark()
            }
            if (alert.status.canMoveTo(AlertStatus.CLOSED)) {
                TextButton(
                    onClick = { confirmClose = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(
                        text =
                            stringResource(
                                if (state.updating == AlertStatus.CLOSED)
                                    R.string.alert_action_saving
                                else R.string.alert_action_close
                            ),
                        color = tataAccentColor(TataNavy),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        // Notes can be added at any status: the follow-up history stays useful after closing.
        TextButton(
            onClick = onAddNote,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.note_add_followup),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = tataAccentColor(TataNavy),
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (noteSaved) {
            Text(
                text = "✓ " + stringResource(R.string.note_saved),
                color = TataSuccess,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        state.feedback?.let { FeedbackText(it) }
    }

    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            title = { Text(stringResource(R.string.alert_close_confirm_title)) },
            text = { Text(stringResource(R.string.alert_close_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClose = false
                        onUpdateStatus(AlertStatus.CLOSED)
                    },
                    enabled = !busy && alert.status.canMoveTo(AlertStatus.CLOSED),
                ) {
                    Text(stringResource(R.string.alert_action_close))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClose = false }) {
                    Text(stringResource(R.string.alert_close_cancel))
                }
            },
        )
    }
}

/** Green "Atendida" of the Figma: a state, not a button, so it is not clickable. */
@Composable
private fun AttendedMark() {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(48.dp)
                .background(TataMint, RoundedCornerShape(28.dp))
                .border(BorderStroke(1.dp, TataSuccess), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "✓ " + stringResource(R.string.alert_action_attended),
            color = TataSuccess,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FeedbackText(feedback: AlertFeedback) {
    val (message, color) =
        when (feedback) {
            is AlertFeedback.StatusChanged ->
                when (feedback.status) {
                    AlertStatus.CLOSED -> R.string.alert_feedback_closed
                    else -> R.string.alert_feedback_attended
                } to TataSuccess
            is AlertFeedback.Failed ->
                when (feedback.problem) {
                    AlertsProblem.NETWORK -> R.string.alerts_error_network
                    AlertsProblem.ACCESS_DENIED -> R.string.alerts_error_access
                    AlertsProblem.NOT_FOUND -> R.string.alerts_error_detail_not_found
                    AlertsProblem.CONFLICT -> R.string.alert_feedback_conflict
                    AlertsProblem.UNKNOWN -> R.string.alert_feedback_failed
                } to TataError
        }
    Text(
        text = stringResource(message),
        color = color,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Polite },
    )
}
