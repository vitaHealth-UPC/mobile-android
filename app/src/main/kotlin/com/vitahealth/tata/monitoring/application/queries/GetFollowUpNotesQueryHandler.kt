package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommand
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQuery
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.NoteTextProblem
import com.vitahealth.tata.monitoring.domain.repositories.NotesRepository
import com.vitahealth.tata.shared.common.result.AppResult

class GetFollowUpNotesQueryHandler(private val repository: NotesRepository) {
    suspend operator fun invoke(query: GetFollowUpNotesQuery): AppResult<List<FollowUpNote>> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) return invalidFollowUp()
        return when (
            val result = repository.notes(query.caregiverId.trim(), query.olderAdultId.trim())
        ) {
            is AppResult.Success ->
                AppResult.Success(result.value.sortedByDescending { it.recordedAt })
            is AppResult.Failure -> result
        }
    }
}

private fun invalidFollowUp() =
    AppResult.Failure(
        message = "No se pudo identificar el seguimiento.",
        code = AlertFailureCodes.INVALID_REFERENCE,
    )
