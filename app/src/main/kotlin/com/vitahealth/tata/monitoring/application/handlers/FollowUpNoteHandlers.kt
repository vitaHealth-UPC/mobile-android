package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.NotesRepository
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommand
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQuery
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.NoteTextProblem
import com.vitahealth.tata.shared.common.result.AppResult

class GetFollowUpNotesQueryHandler(private val repository: NotesRepository) {
    suspend operator fun invoke(query: GetFollowUpNotesQuery): AppResult<List<FollowUpNote>> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) return invalidFollowUp()
        return when (val result = repository.notes(query.caregiverId.trim(), query.olderAdultId.trim())) {
            is AppResult.Success -> AppResult.Success(result.value.sortedByDescending { it.recordedAt })
            is AppResult.Failure -> result
        }
    }
}

/** Registers a note written by the caregiver; the text is trimmed and checked before it is sent. */
class RegisterFollowUpNoteCommandHandler(private val repository: NotesRepository) {
    suspend operator fun invoke(command: RegisterFollowUpNoteCommand): AppResult<FollowUpNote> {
        if (command.caregiverId.isBlank() || command.olderAdultId.isBlank()) return invalidFollowUp()
        when (FollowUpNote.problemWith(command.text)) {
            NoteTextProblem.BLANK -> return AppResult.Failure("Escribe la nota antes de guardarla.", code = AlertFailureCodes.NOTE_BLANK)
            NoteTextProblem.TOO_LONG -> return AppResult.Failure("La nota es demasiado larga.", code = AlertFailureCodes.NOTE_TOO_LONG)
            null -> Unit
        }
        return repository.register(command.caregiverId.trim(), command.olderAdultId.trim(), command.text.trim())
    }
}

private fun invalidFollowUp() = AppResult.Failure(
    message = "No se pudo identificar el seguimiento.",
    code = AlertFailureCodes.INVALID_REFERENCE,
)
