package com.vitahealth.tata.monitoring.application.commands

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.common.result.AppResult

/** Validates the request and delegates to its authorized repository port. */
class CreatePersonalNoteCommandHandler(private val repository: PersonalNotesRepository) {
    suspend operator fun invoke(command: CreatePersonalNoteCommand): AppResult<PersonalNote> {
        val title = command.title.trim()
        val text = command.text.trim()
        if (title.isBlank() || text.isBlank() || title.length > 100 || text.length > 1000) {
            return AppResult.Failure("Invalid personal note", code = "INVALID_NOTE")
        }
        return repository.create(title, text, command.category)
    }
}
