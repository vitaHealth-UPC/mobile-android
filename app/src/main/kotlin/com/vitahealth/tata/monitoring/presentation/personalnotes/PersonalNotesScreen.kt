package com.vitahealth.tata.monitoring.presentation.personalnotes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import java.time.*
import java.time.format.DateTimeFormatter

private val notesInter = FontFamily(Font(R.font.tata_inter))

@Composable
fun PersonalNotesRoute(
    factory: PersonalNotesViewModel.Factory,
    onTab: (AdultTab) -> Unit,
    onSignIn: () -> Unit,
) {
    val model: PersonalNotesViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    PersonalNotesScreen(
        state,
        model::refresh,
        model::filter,
        { model.compose(true) },
        onTab,
        onSignIn,
    )
    if (state.composing)
        PersonalNoteComposer(state.saving, state.saveError, model::save, { model.compose(false) })
}

@Composable
fun PersonalNotesScreen(
    state: PersonalNotesUiState,
    onRetry: () -> Unit,
    onFilter: (PersonalNoteCategory?) -> Unit,
    onAdd: () -> Unit,
    onTab: (AdultTab) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val notes = state.notes.filter { state.category == null || it.category == state.category }
    val current =
        notes.filter { it.recordedAt.atZone(ZoneId.systemDefault()).toLocalDate() == today }
    val recent = notes.filterNot { it in current }
    Column(modifier.fillMaxSize().background(Color(0xFFFBFBFE))) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.personal_notes_title),
                    Modifier.weight(1f),
                    fontFamily = FontFamily(Font(R.font.tata_serif)),
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    color = TataText,
                )
                OutlinedIconButton(
                    onClick = onAdd,
                    Modifier.size(42.dp),
                    border = BorderStroke(1.dp, TataBorder),
                ) {
                    Text("+", color = TataNavy, fontSize = 24.sp)
                }
            }
            Text(
                stringResource(R.string.personal_notes_subtitle),
                color = TataMuted,
                fontFamily = notesInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
            )
            Spacer(Modifier.height(24.dp))
            NotesLabel(stringResource(R.string.personal_notes_today))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteFilter(stringResource(R.string.personal_notes_all), state.category == null) {
                    onFilter(null)
                }
                NoteFilter(
                    stringResource(R.string.adult_tab_medications),
                    state.category == PersonalNoteCategory.MEDICATION,
                ) {
                    onFilter(PersonalNoteCategory.MEDICATION)
                }
                NoteFilter(
                    stringResource(R.string.personal_notes_routine),
                    state.category == PersonalNoteCategory.ROUTINE,
                ) {
                    onFilter(PersonalNoteCategory.ROUTINE)
                }
            }
            Spacer(Modifier.height(18.dp))
            when {
                state.loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                state.error != null -> {
                    Text(stringResource(R.string.personal_notes_load_error), color = TataMuted)
                    TextButton(
                        onClick =
                            if (
                                state.error == "SESSION_REQUIRED" ||
                                    state.error == "OLDER_ADULT_SESSION_REQUIRED"
                            )
                                onSignIn
                            else onRetry
                    ) {
                        Text(
                            stringResource(
                                if (
                                    state.error == "SESSION_REQUIRED" ||
                                        state.error == "OLDER_ADULT_SESSION_REQUIRED"
                                )
                                    R.string.personal_notes_sign_in
                                else R.string.personal_notes_retry
                            )
                        )
                    }
                }
                notes.isEmpty() ->
                    Text(
                        stringResource(R.string.personal_notes_empty),
                        color = TataMuted,
                        fontFamily = notesInter,
                        fontSize = 12.sp,
                    )
                else -> {
                    Box {
                        Box(Modifier.matchParentSize().padding(start = 50.dp, top = 8.dp)) {
                            Box(Modifier.fillMaxHeight().width(2.dp).background(Color(0xFFE5E4EE)))
                        }
                        Column {
                            current.forEach {
                                PersonalNoteCard(it, Color(0xFFF0EBFF), true)
                                Spacer(Modifier.height(16.dp))
                            }
                            if (recent.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                NotesLabel(stringResource(R.string.personal_notes_recent))
                                Spacer(Modifier.height(10.dp))
                                recent.forEachIndexed { index, note ->
                                    PersonalNoteCard(
                                        note,
                                        if (index % 2 == 0) Color.White else Color(0xFFE8F5FC),
                                        false,
                                    )
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Column(
                Modifier.fillMaxWidth()
                    .background(Color(0xFFFFF5DE), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                NotesLabel(stringResource(R.string.personal_notes_tip))
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.personal_notes_tip_text),
                    fontFamily = notesInter,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = TataMuted,
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onAdd,
                Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TataNavy),
                shape = RoundedCornerShape(23.dp),
            ) {
                Text(
                    stringResource(R.string.personal_notes_add),
                    fontFamily = notesInter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        AdultTabBar(
            AdultTab.Notes,
            onTab,
            setOf(AdultTab.Home, AdultTab.Medications, AdultTab.Agenda, AdultTab.Notes),
        )
    }
}

@Composable
private fun NotesLabel(text: String) {
    Text(
        text,
        fontFamily = notesInter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = TataText,
    )
}

@Composable
private fun NoteFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(30.dp)
            .background(if (selected) TataNavy else Color.White, RoundedCornerShape(15.dp))
            .border(1.dp, if (selected) TataNavy else TataBorder, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontFamily = notesInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = if (selected) Color.White else TataMuted,
        )
    }
}

@Composable
private fun PersonalNoteCard(note: PersonalNote, color: Color, highlight: Boolean) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = if (highlight) 126.dp else 96.dp)
            .shadow(5.dp, shape, ambientColor = Color(0x0F1F2645), spotColor = Color(0x0F1F2645))
            .background(color, shape)
            .border(1.dp, TataBorder, shape)
            .padding(15.dp)
    ) {
        if (highlight) {
            Box(
                Modifier.size(36.dp).background(Color(0xFFDDD4FF), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("•", fontSize = 20.sp, color = TataNavy)
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                note.title,
                fontFamily = notesInter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                color = TataText,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                note.text,
                fontFamily = notesInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = TataMuted,
            )
            Spacer(Modifier.height(if (highlight) 24.dp else 10.dp))
            val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
            val date = note.recordedAt.atZone(ZoneId.systemDefault())
            val prefix =
                when (date.toLocalDate()) {
                    LocalDate.now() -> stringResource(R.string.personal_notes_today)
                    LocalDate.now().minusDays(1) ->
                        stringResource(R.string.personal_notes_yesterday)
                    else -> date.format(DateTimeFormatter.ofPattern("EEE", locale))
                }
            Text(
                prefix + " - " + date.format(DateTimeFormatter.ofPattern("h:mm a", locale)),
                fontFamily = notesInter,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = TataNavy,
            )
        }
    }
}

@Composable
private fun PersonalNoteComposer(
    saving: Boolean,
    error: Boolean,
    onSave: (String, String, PersonalNoteCategory) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    var medication by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.personal_notes_add)) },
        text = {
            Column {
                OutlinedTextField(
                    title,
                    { if (it.length <= 100) title = it },
                    label = { Text(stringResource(R.string.personal_notes_subject)) },
                    enabled = !saving,
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    text,
                    { if (it.length <= 1000) text = it },
                    label = { Text(stringResource(R.string.personal_notes_body)) },
                    enabled = !saving,
                    minLines = 3,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(medication, { medication = it }, enabled = !saving)
                    Text(stringResource(R.string.adult_tab_medications))
                }
                if (error)
                    Text(
                        stringResource(R.string.personal_notes_save_error),
                        color = MaterialTheme.colorScheme.error,
                    )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        title,
                        text,
                        if (medication) PersonalNoteCategory.MEDICATION
                        else PersonalNoteCategory.ROUTINE,
                    )
                },
                enabled = !saving && title.isNotBlank() && text.isNotBlank(),
            ) {
                Text(stringResource(R.string.personal_notes_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.personal_notes_cancel))
            }
        },
    )
}
