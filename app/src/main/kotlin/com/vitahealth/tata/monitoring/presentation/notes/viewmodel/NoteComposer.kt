package com.vitahealth.tata.monitoring.presentation.notes.viewmodel

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommand
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.NoteTextProblem
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NoteProblem {
    BLANK,
    TOO_LONG,
    REJECTED,
    NETWORK,
    ACCESS_DENIED,
    NOT_FOUND,
    UNKNOWN,
}

internal fun noteProblemOf(code: String?): NoteProblem =
    when (code) {
        AlertFailureCodes.NOTE_BLANK -> NoteProblem.BLANK
        AlertFailureCodes.NOTE_TOO_LONG -> NoteProblem.TOO_LONG
        AlertFailureCodes.NOTE_REJECTED -> NoteProblem.REJECTED
        AlertFailureCodes.NETWORK -> NoteProblem.NETWORK
        AlertFailureCodes.ACCESS_DENIED,
        AlertFailureCodes.SESSION_REQUIRED,
        "CARE_RELATIONSHIP_REQUIRED",
        "SESSION_CHANGED" -> NoteProblem.ACCESS_DENIED
        AlertFailureCodes.NOT_FOUND -> NoteProblem.NOT_FOUND
        else -> NoteProblem.UNKNOWN
    }

/**
 * [saved] stays true after a note is stored, so the screen can show "Nota guardada" until the next
 * note.
 */
data class NoteComposerUiState(
    val open: Boolean = false,
    val saving: Boolean = false,
    val error: NoteProblem? = null,
    val saved: Boolean = false,
)

/**
 * Write-a-note flow shared by the notes screen and the alert detail. It lives inside their view
 * models and runs on their scope.
 */
class NoteComposer(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val handler: RegisterFollowUpNoteCommandHandler,
    private val scope: CoroutineScope,
    private val context: CoroutineContext,
    private val onAccessDenied: () -> Unit = {},
) {
    private val mutableState = MutableStateFlow(NoteComposerUiState())
    val state: StateFlow<NoteComposerUiState> = mutableState.asStateFlow()
    private var saveJob: Job? = null

    /**
     * Discards protected draft UI and prevents a late save from reopening it after access is lost.
     */
    fun clear() {
        saveJob?.cancel()
        mutableState.value = NoteComposerUiState()
    }

    fun open() {
        if (mutableState.value.saving) return
        mutableState.value = NoteComposerUiState(open = true)
    }

    fun dismiss() {
        if (!mutableState.value.saving)
            mutableState.value = mutableState.value.copy(open = false, error = null)
    }

    fun save(text: String, onSaved: (FollowUpNote) -> Unit = {}) {
        val current = mutableState.value
        if (!current.open || current.saving) return
        when (FollowUpNote.problemWith(text)) {
            NoteTextProblem.BLANK -> {
                mutableState.value = current.copy(error = NoteProblem.BLANK)
                return
            }
            NoteTextProblem.TOO_LONG -> {
                mutableState.value = current.copy(error = NoteProblem.TOO_LONG)
                return
            }
            null -> Unit
        }
        mutableState.value = current.copy(saving = true, error = null)
        saveJob =
            scope.launch(context) {
                val result = handler(RegisterFollowUpNoteCommand(caregiverId, olderAdultId, text))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> {
                        mutableState.value = NoteComposerUiState(saved = true)
                        onSaved(result.value)
                    }
                    is AppResult.Failure -> {
                        val problem = noteProblemOf(result.code)
                        mutableState.value =
                            mutableState.value.copy(saving = false, error = problem)
                        if (problem == NoteProblem.ACCESS_DENIED) onAccessDenied()
                    }
                }
            }
    }
}
