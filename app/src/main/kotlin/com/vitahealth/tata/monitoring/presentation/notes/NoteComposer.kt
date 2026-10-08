package com.vitahealth.tata.monitoring.presentation.notes

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommand
import com.vitahealth.tata.monitoring.application.handlers.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.NoteTextProblem
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class NoteProblem { BLANK, TOO_LONG, REJECTED, NETWORK, ACCESS_DENIED, NOT_FOUND, UNKNOWN }

internal fun noteProblemOf(code: String?): NoteProblem = when (code) {
    AlertFailureCodes.NOTE_BLANK -> NoteProblem.BLANK
    AlertFailureCodes.NOTE_TOO_LONG -> NoteProblem.TOO_LONG
    AlertFailureCodes.NOTE_REJECTED -> NoteProblem.REJECTED
    AlertFailureCodes.NETWORK -> NoteProblem.NETWORK
    AlertFailureCodes.ACCESS_DENIED -> NoteProblem.ACCESS_DENIED
    AlertFailureCodes.NOT_FOUND -> NoteProblem.NOT_FOUND
    else -> NoteProblem.UNKNOWN
}

/** [saved] stays true after a note is stored, so the screen can show "Nota guardada" until the next note. */
data class NoteComposerUiState(
    val open: Boolean = false,
    val saving: Boolean = false,
    val error: NoteProblem? = null,
    val saved: Boolean = false,
)

/**
 * Write-a-note flow shared by the notes screen and the alert detail. It lives inside their view models
 * and runs on their scope.
 */
class NoteComposer(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val handler: RegisterFollowUpNoteCommandHandler,
    private val scope: CoroutineScope,
    private val context: CoroutineContext,
) {
    private val mutableState = MutableStateFlow(NoteComposerUiState())
    val state: StateFlow<NoteComposerUiState> = mutableState.asStateFlow()

    fun open() {
        mutableState.value = NoteComposerUiState(open = true)
    }

    fun dismiss() {
        if (!mutableState.value.saving) mutableState.value = mutableState.value.copy(open = false, error = null)
    }

    fun save(text: String, onSaved: (FollowUpNote) -> Unit = {}) {
        val current = mutableState.value
        if (!current.open || current.saving) return
        when (FollowUpNote.problemWith(text)) {
            NoteTextProblem.BLANK -> { mutableState.value = current.copy(error = NoteProblem.BLANK); return }
            NoteTextProblem.TOO_LONG -> { mutableState.value = current.copy(error = NoteProblem.TOO_LONG); return }
            null -> Unit
        }
        mutableState.value = current.copy(saving = true, error = null)
        scope.launch(context) {
            when (val result = handler(RegisterFollowUpNoteCommand(caregiverId, olderAdultId, text))) {
                is AppResult.Success -> {
                    mutableState.value = NoteComposerUiState(saved = true)
                    onSaved(result.value)
                }
                is AppResult.Failure -> mutableState.value = mutableState.value.copy(saving = false, error = noteProblemOf(result.code))
            }
        }
    }
}
