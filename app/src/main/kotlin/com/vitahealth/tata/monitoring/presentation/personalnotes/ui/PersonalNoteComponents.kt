package com.vitahealth.tata.monitoring.presentation.personalnotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal val notesInter = FontFamily(Font(R.font.tata_inter))

@Composable
internal fun NotesLabel(text: String) {
    Text(
        text,
        fontFamily = notesInter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = tataTextColor(),
    )
}

@Composable
internal fun NoteFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(30.dp)
            .background(
                if (selected) tataAccentColor(TataNavy) else tataCardColor(),
                RoundedCornerShape(15.dp),
            )
            .border(
                1.dp,
                if (selected) tataAccentColor(TataNavy) else tataOutlineColor(),
                RoundedCornerShape(15.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontFamily = notesInter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else tataMutedColor(),
        )
    }
}

@Composable
internal fun PersonalNoteCard(note: PersonalNote, color: Color, highlight: Boolean) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = if (highlight) 126.dp else 96.dp)
            .shadow(5.dp, shape, ambientColor = Color(0x0F1F2645), spotColor = Color(0x0F1F2645))
            .background(tataTonalColor(color), shape)
            .border(1.dp, tataOutlineColor(), shape)
            .padding(15.dp)
    ) {
        if (highlight) {
            Box(
                Modifier.size(36.dp)
                    .background(tataTonalColor(Color(0xFFDDD4FF)), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("•", fontSize = 20.sp, color = tataAccentColor(TataNavy))
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
                color = tataTextColor(),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                note.text,
                fontFamily = notesInter,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = tataMutedColor(),
            )
            Spacer(Modifier.height(if (highlight) 24.dp else 10.dp))
            val locale = LocalConfiguration.current.locales[0]
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
                color = tataAccentColor(TataNavy),
            )
        }
    }
}

@Composable
internal fun PersonalNoteComposer(
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
