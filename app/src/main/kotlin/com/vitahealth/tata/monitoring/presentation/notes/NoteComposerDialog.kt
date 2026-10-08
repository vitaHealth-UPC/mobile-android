package com.vitahealth.tata.monitoring.presentation.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsSecondaryText
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.tataTextColor

/** There is no Figma frame for writing a note; the dialog follows the confirm dialog of the alert detail. */
@Composable
internal fun NoteComposerDialog(
    state: NoteComposerUiState,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!state.open) return
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    // Typing past the limit is allowed so pasted text is not cut silently; saving explains it.
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.note_dialog_label)) },
                    enabled = !state.saving,
                    isError = state.error == NoteProblem.BLANK || state.error == NoteProblem.TOO_LONG,
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                )
                Text(
                    text = stringResource(R.string.note_dialog_counter, text.trim().length, FollowUpNote.MAX_LENGTH),
                    color = if (text.trim().length > FollowUpNote.MAX_LENGTH) TataError else AlertsSecondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                state.error?.let { problem ->
                    Text(
                        text = noteErrorText(problem),
                        color = TataError,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = !state.saving) {
                Text(
                    text = stringResource(if (state.saving) R.string.alert_action_saving else R.string.note_dialog_save),
                    color = TataNavy,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.saving) {
                Text(stringResource(R.string.note_dialog_cancel), color = tataTextColor())
            }
        },
    )
}

@Composable
private fun noteErrorText(problem: NoteProblem): String = when (problem) {
    NoteProblem.BLANK -> stringResource(R.string.note_error_blank)
    NoteProblem.TOO_LONG -> stringResource(R.string.note_error_too_long, FollowUpNote.MAX_LENGTH)
    NoteProblem.REJECTED -> stringResource(R.string.note_error_rejected)
    NoteProblem.NETWORK -> stringResource(R.string.alerts_error_network)
    NoteProblem.ACCESS_DENIED -> stringResource(R.string.alerts_error_access)
    NoteProblem.NOT_FOUND -> stringResource(R.string.alerts_error_list_not_found)
    NoteProblem.UNKNOWN -> stringResource(R.string.note_error_save)
}
