package com.vitahealth.tata.monitoring.presentation.notes

import com.vitahealth.tata.monitoring.FakeAlertsRepository
import com.vitahealth.tata.monitoring.FakeContactRepository
import com.vitahealth.tata.monitoring.FakeNotesRepository
import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFollowUpNotesQueryHandler
import com.vitahealth.tata.monitoring.failure
import com.vitahealth.tata.monitoring.note
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NoteProblem
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** US-30: list and write follow-up notes, from the notes tab and from an alert. */
class NotesViewModelTest {
    private val repository = FakeNotesRepository()

    private fun model() =
        NotesViewModel(
            "caregiver",
            "adult",
            GetFollowUpNotesQueryHandler(repository),
            RegisterFollowUpNoteCommandHandler(repository),
            Dispatchers.Unconfined,
        )

    @Test
    fun startsLoadingUntilTheNotesArrive() {
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        repository.list = AppResult.Success(listOf(note()))
        val model = model()

        model.refresh()
        assertEquals(NotesUiState.Loading, model.state.value)

        gate.complete(Unit)
        assertTrue(model.state.value is NotesUiState.Content)
    }

    @Test
    fun showsTheNotesOrTheEmptyState() {
        repository.list = AppResult.Success(listOf(note(id = 1)))
        val withNotes = model().also { it.refresh() }
        assertEquals(
            listOf(1L),
            (withNotes.state.value as NotesUiState.Content).notes.map { it.id },
        )

        repository.list = AppResult.Success(emptyList())
        val empty = model().also { it.refresh() }
        assertEquals(NotesUiState.Empty, empty.state.value)
    }

    @Test
    fun explainsEachReadFailure() {
        mapOf(
                AlertFailureCodes.NETWORK to AlertsProblem.NETWORK,
                AlertFailureCodes.ACCESS_DENIED to AlertsProblem.ACCESS_DENIED,
                AlertFailureCodes.NOT_FOUND to AlertsProblem.NOT_FOUND,
            )
            .forEach { (code, problem) ->
                repository.list = failure(code)
                val model = model().also { it.refresh() }
                assertEquals(code, NotesUiState.Error(problem), model.state.value)
            }
    }

    @Test
    fun aSavedNoteGoesOnTopAndClosesTheDialog() {
        repository.list = AppResult.Success(listOf(note(id = 1)))
        repository.registered = AppResult.Success(note(id = 9, text = "Called her"))
        val model = model().also { it.refresh() }

        model.composer.open()
        model.saveNote("Called her")

        assertEquals(
            listOf(9L, 1L),
            (model.state.value as NotesUiState.Content).notes.map { it.id },
        )
        assertFalse(model.composer.state.value.open)
        assertTrue(model.composer.state.value.saved)
    }

    @Test
    fun theFirstNoteReplacesTheEmptyState() {
        repository.list = AppResult.Success(emptyList())
        val model = model().also { it.refresh() }

        model.composer.open()
        model.saveNote("Called her")

        assertTrue(model.state.value is NotesUiState.Content)
    }

    @Test
    fun blankAndLongNotesStayInTheDialog() {
        val model = model()
        model.composer.open()

        model.saveNote("   ")
        assertEquals(NoteProblem.BLANK, model.composer.state.value.error)

        model.saveNote("a".repeat(1001))
        assertEquals(NoteProblem.TOO_LONG, model.composer.state.value.error)
        assertTrue(model.composer.state.value.open)
        assertTrue(repository.requests.none { it.startsWith("register") })
    }

    @Test
    fun aFailedSaveKeepsTheDialogOpenWithTheReason() {
        mapOf(
                AlertFailureCodes.NETWORK to NoteProblem.NETWORK,
                AlertFailureCodes.NOTE_REJECTED to NoteProblem.REJECTED,
                AlertFailureCodes.NOT_FOUND to NoteProblem.NOT_FOUND,
            )
            .forEach { (code, problem) ->
                repository.registered = failure(code)
                val model = model()
                model.composer.open()

                model.saveNote("Called her")

                assertEquals(code, problem, model.composer.state.value.error)
                assertTrue(code, model.composer.state.value.open)
                assertFalse(code, model.composer.state.value.saving)
            }
    }

    @Test
    fun showsSavingAndIgnoresASecondSave() {
        val model = model()
        model.composer.open()
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        model.saveNote("Called her")
        assertTrue(model.composer.state.value.saving)
        model.composer.open()
        model.refresh()
        assertTrue(model.composer.state.value.saving)
        model.saveNote("Called her again")
        gate.complete(Unit)

        assertEquals(1, repository.requests.count { it.startsWith("register") })
        assertTrue(repository.requests.none { it.startsWith("notes:") })
    }

    @Test
    fun theAlertDetailCanAddANote() {
        val alerts = FakeAlertsRepository().apply { detail = AppResult.Success(alert(id = 1)) }
        val detail =
            AlertDetailViewModel(
                "caregiver",
                "adult",
                1,
                GetAlertDetailQueryHandler(alerts),
                UpdateAlertStatusCommandHandler(alerts),
                RegisterFollowUpNoteCommandHandler(repository),
                GetContactOptionQueryHandler(FakeContactRepository()),
                Dispatchers.Unconfined,
            )

        detail.noteComposer.open()
        detail.noteComposer.save("Called her after the alert")

        assertTrue(detail.noteComposer.state.value.saved)
        assertEquals(
            "register:caregiver:adult:Called her after the alert",
            repository.requests.single(),
        )
    }

    @Test
    fun deniedSaveDiscardsPreviouslyVisibleNotesAndEditor() {
        listOf(
                AlertFailureCodes.ACCESS_DENIED,
                AlertFailureCodes.SESSION_REQUIRED,
                "SESSION_CHANGED",
            )
            .forEach { code ->
                repository.list = AppResult.Success(listOf(note(id = 1)))
                val model = model().also { it.refresh() }
                repository.registered = failure(code)
                model.composer.open()
                model.saveNote("Seguimiento realizado")
                assertEquals(NotesUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
                assertFalse(model.composer.state.value.open)
                assertFalse(model.composer.state.value.saved)
            }
    }

    @Test
    fun linkedProfileIsRemoteDataAndClearedWhenAccessIsRevoked() {
        repository.list = AppResult.Success(listOf(note(id = 1)))
        val reader =
            MonitoredPersonReader { caregiverId, adultId ->
                assertEquals("caregiver", caregiverId)
                assertEquals("adult", adultId)
                AppResult.Success(MonitoredPerson("adult", "Rosa Vargas", null))
            }
        val model =
            NotesViewModel(
                "caregiver",
                "adult",
                GetFollowUpNotesQueryHandler(repository),
                RegisterFollowUpNoteCommandHandler(repository),
                Dispatchers.Unconfined,
                reader,
            )

        model.refresh()
        assertEquals("Rosa Vargas", model.person.value?.fullName)

        repository.list = failure(AlertFailureCodes.ACCESS_DENIED)
        model.refresh()
        assertNull(model.person.value)
        assertEquals(NotesUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
    }

    @Test
    fun profilePermissionFailureAlsoClearsAuthorizedNotes() {
        repository.list = AppResult.Success(listOf(note(id = 1)))
        val denied = MonitoredPersonReader { _, _ -> failure(AlertFailureCodes.ACCESS_DENIED) }
        val model =
            NotesViewModel(
                "caregiver",
                "adult",
                GetFollowUpNotesQueryHandler(repository),
                RegisterFollowUpNoteCommandHandler(repository),
                Dispatchers.Unconfined,
                denied,
            )
        model.refresh()
        assertNull(model.person.value)
        assertEquals(NotesUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
    }

    @Test
    fun deniedRefreshAlsoDiscardsTheOpenEditor() {
        repository.list = AppResult.Success(listOf(note(id = 1)))
        val model = model().also { it.refresh() }
        model.composer.open()
        repository.list = failure(AlertFailureCodes.ACCESS_DENIED)
        model.refresh()
        assertEquals(NotesUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
        assertFalse(model.composer.state.value.open)
    }
}
