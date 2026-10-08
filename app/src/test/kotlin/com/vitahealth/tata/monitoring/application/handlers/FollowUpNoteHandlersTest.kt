package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.FakeNotesRepository
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommand
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQuery
import com.vitahealth.tata.monitoring.domain.model.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.NoteTextProblem
import com.vitahealth.tata.monitoring.note
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowUpNoteHandlersTest {
    private val repository = FakeNotesRepository()

    @Test fun noteTextRulesFollowTheSwaggerLimit() {
        assertEquals(NoteTextProblem.BLANK, FollowUpNote.problemWith("   "))
        assertNull(FollowUpNote.problemWith("a".repeat(FollowUpNote.MAX_LENGTH)))
        assertNull(FollowUpNote.problemWith("  " + "a".repeat(FollowUpNote.MAX_LENGTH) + "  "))
        assertEquals(NoteTextProblem.TOO_LONG, FollowUpNote.problemWith("a".repeat(FollowUpNote.MAX_LENGTH + 1)))
    }

    @Test fun listsTheMostRecentNoteFirst() = runBlocking {
        repository.list = AppResult.Success(listOf(note(id = 1, recordedAt = "2026-10-04T10:00:00Z"), note(id = 2)))

        val result = GetFollowUpNotesQueryHandler(repository)(GetFollowUpNotesQuery("caregiver", "adult"))

        assertEquals(listOf(2L, 1L), (result as AppResult.Success).value.map { it.id })
    }

    @Test fun registersTheTrimmedText() = runBlocking {
        val result = RegisterFollowUpNoteCommandHandler(repository)(RegisterFollowUpNoteCommand("caregiver", "adult", "  Called her  "))

        assertTrue(result is AppResult.Success)
        assertEquals("register:caregiver:adult:Called her", repository.requests.single())
    }

    @Test fun invalidTextNeverReachesTheServer() = runBlocking {
        val handler = RegisterFollowUpNoteCommandHandler(repository)

        val blank = handler(RegisterFollowUpNoteCommand("caregiver", "adult", " "))
        val tooLong = handler(RegisterFollowUpNoteCommand("caregiver", "adult", "a".repeat(1001)))

        assertEquals(AlertFailureCodes.NOTE_BLANK, (blank as AppResult.Failure).code)
        assertEquals(AlertFailureCodes.NOTE_TOO_LONG, (tooLong as AppResult.Failure).code)
        assertTrue(repository.requests.isEmpty())
    }
}
