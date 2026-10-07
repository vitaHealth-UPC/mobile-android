package com.vitahealth.tata.monitoring.presentation.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.R
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsLoading
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsProblemCard
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsSecondaryText
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsTitle
import com.vitahealth.tata.monitoring.presentation.alerts.rememberAlertDateFormatter
import com.vitahealth.tata.shared.design.components.CaregiverTab
import com.vitahealth.tata.shared.design.components.CaregiverTabBar
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataSuccess
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.Instant

@Composable
fun NotesRoute(
    factory: NotesViewModel.Factory,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    val model: NotesViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val composer by model.composer.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) model.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    NotesScreen(
        state = state,
        caregiverId = model.caregiverId,
        noteSaved = composer.saved,
        onRetry = model::refresh,
        onAddNote = model.composer::open,
        onTabSelected = onTabSelected,
    )
    NoteComposerDialog(composer, onSave = model::saveNote, onDismiss = model.composer::dismiss)
}

@Composable
fun NotesScreen(
    state: NotesUiState,
    caregiverId: String,
    noteSaved: Boolean = false,
    onRetry: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
) {
    val formatter = rememberAlertDateFormatter()
    Column(modifier = Modifier.fillMaxSize().background(TataSurface)) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                AlertsTitle(stringResource(R.string.notes_title))
                Text(
                    text = stringResource(R.string.notes_subtitle),
                    color = AlertsSecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (noteSaved) {
                item {
                    Text(
                        text = "✓ " + stringResource(R.string.note_saved),
                        color = TataSuccess,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
            when (state) {
                NotesUiState.Loading -> item { AlertsLoading(stringResource(R.string.notes_loading)) }
                NotesUiState.Empty -> item { EmptyNotesCard() }
                is NotesUiState.Error -> item {
                    val problem = if (state.problem == AlertsProblem.CONFLICT) AlertsProblem.UNKNOWN else state.problem
                    if (problem == AlertsProblem.UNKNOWN) {
                        TataCard(modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.notes_error_unknown), color = tataTextColor())
                            TataButton(stringResource(R.string.alerts_retry), onRetry, Modifier.padding(top = 16.dp))
                        }
                    } else {
                        AlertsProblemCard(problem, R.string.alerts_error_list_not_found, onRetry)
                    }
                }
                is NotesUiState.Content -> {
                    item {
                        Text(
                            text = stringResource(R.string.notes_section),
                            color = tataTextColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(state.notes, key = { it.id }) { note ->
                        NoteCard(note, mine = note.familiarId == caregiverId, recordedAt = formatter.format(note.recordedAt))
                    }
                }
            }
        }
        // Without a link or an active follow-up the backend rejects new notes too, so the button is hidden.
        if (state !is NotesUiState.Error || state.problem !in setOf(AlertsProblem.ACCESS_DENIED, AlertsProblem.NOT_FOUND)) {
            TataButton(
                text = stringResource(R.string.notes_add),
                onClick = onAddNote,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
            )
        }
        CaregiverTabBar(selected = CaregiverTab.Notes, onSelect = onTabSelected)
    }
}

@Composable
private fun EmptyNotesCard() {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = TataMint) {
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
private fun NoteCard(note: FollowUpNote, mine: Boolean, recordedAt: String) {
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = if (mine) TataLavender else Color.White) {
        Text(text = note.text, color = tataTextColor(), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(
                R.string.notes_meta,
                stringResource(if (mine) R.string.notes_author_you else R.string.notes_author_other),
                recordedAt,
            ),
            color = AlertsSecondaryText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private val previewNotes = listOf(
    FollowUpNote(2, "La llamé y ya había tomado la pastilla.", Instant.parse("2026-10-05T14:10:00Z"), "caregiver-1"),
    FollowUpNote(1, "Prefiere una llamada si no responde al segundo recordatorio.", Instant.parse("2026-10-04T20:00:00Z"), "caregiver-2"),
)

@Preview(name = "Notas con datos", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesContentPreview() {
    TataTheme { NotesScreen(NotesUiState.Content(previewNotes), caregiverId = "caregiver-1", noteSaved = true) }
}

@Preview(name = "Sin notas", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesEmptyPreview() {
    TataTheme { NotesScreen(NotesUiState.Empty, caregiverId = "caregiver-1") }
}

@Preview(name = "Notas sin red", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesErrorPreview() {
    TataTheme { NotesScreen(NotesUiState.Error(AlertsProblem.NETWORK), caregiverId = "caregiver-1") }
}

@Preview(name = "Nueva nota con error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NoteComposerPreview() {
    TataTheme { NoteComposerDialog(NoteComposerUiState(open = true, error = NoteProblem.BLANK), onSave = {}, onDismiss = {}) }
}
