package com.vitahealth.tata.monitoring.presentation.notes.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsLoading
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsProblemCard
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsSecondaryText
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertsTitle
import com.vitahealth.tata.monitoring.presentation.alerts.ui.rememberAlertDateFormatter
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.notes.ui.EmptyNotesCard
import com.vitahealth.tata.monitoring.presentation.notes.ui.NoteCard
import com.vitahealth.tata.monitoring.presentation.notes.ui.NoteComposerDialog
import com.vitahealth.tata.monitoring.presentation.notes.ui.NotesPersonCard
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NoteComposerUiState
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NoteProblem
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataInterFontFamily
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataInterTypography
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.Instant

@Composable
fun NotesScreen(
    state: NotesUiState,
    caregiverId: String,
    noteSaved: Boolean = false,
    onRetry: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onTabSelected: (CaregiverTab) -> Unit = {},
    person: MonitoredPerson? = null,
) {
    val formatter = rememberAlertDateFormatter()
    val authorized = state is NotesUiState.Content || state == NotesUiState.Empty
    val addLabel = stringResource(R.string.notes_add)
    TataInterTypography {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = TataInterFontFamily,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        ) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (LocalTataAccessibility.current.darkTheme)
                                    listOf(tataBackgroundColor(), tataCardColor(), tataBackgroundColor())
                                else listOf(Color.White, TataSurface, Color(0xFFFCFBFF))
                            )
                        )
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AlertsTitle(stringResource(R.string.notes_title))
                            if (authorized)
                                OutlinedIconButton(
                                    onClick = onAddNote,
                                    border = BorderStroke(1.dp, tataOutlineColor(Color(0xFFE3E3F0))),
                                    colors = IconButtonDefaults.outlinedIconButtonColors(
                                        containerColor = tataCardColor(),
                                        contentColor = tataAccentColor(Color(0xFF1A336B)),
                                    ),
                                    modifier =
                                        Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                            .semantics { contentDescription = addLabel },
                                ) {
                                    Text("+", fontSize = 24.sp, color = tataTextColor())
                                }
                        }
                        Text(
                            text =
                                if (person != null && authorized)
                                    stringResource(R.string.notes_subtitle_person, person.fullName)
                                else stringResource(R.string.notes_subtitle),
                            color = AlertsSecondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (
                        person != null &&
                            (state is NotesUiState.Content || state == NotesUiState.Empty)
                    ) {
                        item { NotesPersonCard(person) }
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
                        NotesUiState.Loading ->
                            item { AlertsLoading(stringResource(R.string.notes_loading)) }
                        NotesUiState.Empty -> item { EmptyNotesCard() }
                        is NotesUiState.Error ->
                            item {
                                val problem =
                                    if (state.problem == AlertsProblem.CONFLICT)
                                        AlertsProblem.UNKNOWN
                                    else state.problem
                                if (problem == AlertsProblem.UNKNOWN) {
                                    TataCard(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            stringResource(R.string.notes_error_unknown),
                                            color = tataTextColor(),
                                        )
                                        TataButton(
                                            stringResource(R.string.alerts_retry),
                                            onRetry,
                                            Modifier.padding(top = 16.dp),
                                        )
                                    }
                                } else {
                                    AlertsProblemCard(
                                        problem,
                                        R.string.alerts_error_list_not_found,
                                        onRetry,
                                    )
                                }
                            }
                        is NotesUiState.Content -> {
                            item {
                                Text(
                                    text = stringResource(R.string.notes_section),
                                    color = tataTextColor(),
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            itemsIndexed(state.notes, key = { _, note -> note.id }) { index, note ->
                                NoteCard(
                                    note,
                                    mine = note.familiarId == caregiverId,
                                    recordedAt = formatter.format(note.recordedAt),
                                    highlighted = index == 0,
                                )
                            }
                            item {
                                TataCard(Modifier.fillMaxWidth(), containerColor = tataTonalColor(TataMint)) {
                                    Text(
                                        stringResource(R.string.notes_shared_history),
                                        color = tataTextColor(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        stringResource(R.string.notes_shared_history_body),
                                        color = AlertsSecondaryText,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                if (authorized) {
                    TataButton(
                        text = stringResource(R.string.notes_add),
                        onClick = onAddNote,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                    )
                }
                CaregiverTabBar(selected = CaregiverTab.Notes, onSelect = onTabSelected)
            }
        }
    }
}

private val previewNotes =
    listOf(
        FollowUpNote(
            2,
            "La llamé y ya había tomado la pastilla.",
            Instant.parse("2026-10-05T14:10:00Z"),
            "caregiver-1",
        ),
        FollowUpNote(
            1,
            "Prefiere una llamada si no responde al segundo recordatorio.",
            Instant.parse("2026-10-04T20:00:00Z"),
            "caregiver-2",
        ),
    )

@Preview(name = "Notas con datos", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesContentPreview() {
    TataTheme {
        NotesScreen(
            NotesUiState.Content(previewNotes),
            caregiverId = "caregiver-1",
            noteSaved = true,
        )
    }
}

@Preview(name = "Sin notas", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesEmptyPreview() {
    TataTheme { NotesScreen(NotesUiState.Empty, caregiverId = "caregiver-1") }
}

@Preview(name = "Notas sin red", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NotesErrorPreview() {
    TataTheme {
        NotesScreen(NotesUiState.Error(AlertsProblem.NETWORK), caregiverId = "caregiver-1")
    }
}

@Preview(name = "Nueva nota con error", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NoteComposerPreview() {
    TataTheme {
        NoteComposerDialog(
            NoteComposerUiState(open = true, error = NoteProblem.BLANK),
            onSave = {},
            onDismiss = {},
        )
    }
}
