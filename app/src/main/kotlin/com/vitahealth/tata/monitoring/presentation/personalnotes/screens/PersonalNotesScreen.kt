package com.vitahealth.tata.monitoring.presentation.personalnotes.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.presentation.personalnotes.ui.NoteFilter
import com.vitahealth.tata.monitoring.presentation.personalnotes.ui.NotesLabel
import com.vitahealth.tata.monitoring.presentation.personalnotes.ui.PersonalNoteCard
import com.vitahealth.tata.monitoring.presentation.personalnotes.ui.notesInter
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.LocalDate
import java.time.ZoneId

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
    Column(
        modifier.fillMaxSize().background(
            if (LocalTataAccessibility.current.darkTheme) tataBackgroundColor()
            else Color(0xFFFBFBFE)
        )
    ) {
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
                    color = tataTextColor(),
                )
                OutlinedIconButton(
                    onClick = onAdd,
                    enabled = !state.requiresSignIn,
                    modifier = Modifier.size(42.dp),
                    border = BorderStroke(1.dp, tataOutlineColor()),
                ) {
                    Text("+", color = tataAccentColor(TataNavy), fontSize = 24.sp)
                }
            }
            Text(
                stringResource(R.string.personal_notes_subtitle),
                color = tataMutedColor(),
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
                    Text(stringResource(R.string.personal_notes_load_error), color = tataMutedColor())
                    TextButton(onClick = if (state.requiresSignIn) onSignIn else onRetry) {
                        Text(
                            stringResource(
                                if (state.requiresSignIn) R.string.personal_notes_sign_in
                                else R.string.personal_notes_retry
                            )
                        )
                    }
                }
                notes.isEmpty() ->
                    Text(
                        stringResource(R.string.personal_notes_empty),
                        color = tataMutedColor(),
                        fontFamily = notesInter,
                        fontSize = 12.sp,
                    )
                else -> {
                    Box {
                        Box(Modifier.matchParentSize().padding(start = 50.dp, top = 8.dp)) {
                            Box(Modifier.fillMaxHeight().width(2.dp).background(tataOutlineColor(Color(0xFFE5E4EE))))
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
                    .background(tataTonalColor(Color(0xFFFFF5DE)), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                NotesLabel(stringResource(R.string.personal_notes_tip))
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.personal_notes_tip_text),
                    fontFamily = notesInter,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = tataMutedColor(),
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onAdd,
                Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tataAccentColor(TataNavy)),
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
