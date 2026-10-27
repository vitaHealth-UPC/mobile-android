package com.vitahealth.tata.monitoring.presentation.alerts

import com.vitahealth.tata.monitoring.FakeAlertsRepository
import com.vitahealth.tata.monitoring.FakeContactRepository
import com.vitahealth.tata.monitoring.FakeNotesRepository
import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommand
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.canMoveTo
import com.vitahealth.tata.monitoring.failure
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFeedback
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** US-31: attend or close an alert from its detail. */
class AlertFollowUpViewModelTest {
    private val repository = FakeAlertsRepository()
    private val notes = FakeNotesRepository()

    private fun model(status: AlertStatus = AlertStatus.OPEN): AlertDetailViewModel {
        repository.detail = AppResult.Success(alert(id = 1, status = status))
        return AlertDetailViewModel(
            "caregiver",
            "adult",
            1,
            GetAlertDetailQueryHandler(repository),
            UpdateAlertStatusCommandHandler(repository),
            RegisterFollowUpNoteCommandHandler(notes),
            GetContactOptionQueryHandler(FakeContactRepository()),
            Dispatchers.Unconfined,
        )
    }

    private fun content(model: AlertDetailViewModel) =
        model.state.value as AlertDetailUiState.Content

    @Test
    fun allowedMovesFollowTheBackendRules() {
        assertTrue(AlertStatus.OPEN.canMoveTo(AlertStatus.ATTENDED))
        assertTrue(AlertStatus.OPEN.canMoveTo(AlertStatus.CLOSED))
        assertTrue(AlertStatus.ATTENDED.canMoveTo(AlertStatus.CLOSED))
        assertFalse(AlertStatus.ATTENDED.canMoveTo(AlertStatus.OPEN))
        assertFalse(AlertStatus.CLOSED.canMoveTo(AlertStatus.ATTENDED))
        assertFalse(AlertStatus.CLOSED.canMoveTo(AlertStatus.OPEN))
    }

    @Test
    fun theCommandNeverSendsOpen() = runBlocking {
        val result =
            UpdateAlertStatusCommandHandler(repository)(
                UpdateAlertStatusCommand("c", "a", 1, AlertStatus.OPEN)
            )

        assertEquals(AlertFailureCodes.STATUS_NOT_ACCEPTED, (result as AppResult.Failure).code)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun markingAsAttendedShowsTheNewStatus() {
        val model = model()
        repository.update = AppResult.Success(alert(id = 1, status = AlertStatus.ATTENDED))

        model.updateStatus(AlertStatus.ATTENDED)

        assertEquals(AlertStatus.ATTENDED, content(model).alert.status)
        assertEquals(AlertFeedback.StatusChanged(AlertStatus.ATTENDED), content(model).feedback)
        assertEquals("update:caregiver:adult:1:ATTENDED", repository.requests.last())
    }

    @Test
    fun closingAnAttendedAlertShowsItClosed() {
        val model = model(AlertStatus.ATTENDED)
        repository.update = AppResult.Success(alert(id = 1, status = AlertStatus.CLOSED))

        model.updateStatus(AlertStatus.CLOSED)

        assertEquals(AlertStatus.CLOSED, content(model).alert.status)
        assertEquals(AlertFeedback.StatusChanged(AlertStatus.CLOSED), content(model).feedback)
    }

    @Test
    fun showsTheStatusBeingSentUntilTheServerAnswers() {
        val model = model()
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        repository.update = AppResult.Success(alert(id = 1, status = AlertStatus.ATTENDED))

        model.updateStatus(AlertStatus.ATTENDED)
        assertEquals(AlertStatus.ATTENDED, content(model).updating)

        model.updateStatus(AlertStatus.CLOSED)
        gate.complete(Unit)
        assertEquals(1, repository.requests.count { it.startsWith("update") })
    }

    @Test
    fun ignoresMovesTheAlertCannotMake() {
        val model = model(AlertStatus.CLOSED)

        model.updateStatus(AlertStatus.ATTENDED)

        assertTrue(repository.requests.none { it.startsWith("update") })
    }

    @Test
    fun aConflictReloadsTheCurrentAlert() {
        val model = model()
        repository.update = failure(AlertFailureCodes.STATUS_CONFLICT)
        repository.detail = AppResult.Success(alert(id = 1, status = AlertStatus.CLOSED))

        model.updateStatus(AlertStatus.ATTENDED)

        assertEquals(AlertStatus.CLOSED, content(model).alert.status)
        assertEquals(AlertFeedback.Failed(AlertsProblem.CONFLICT), content(model).feedback)
    }

    @Test
    fun otherFailuresKeepTheAlertAndExplainWhy() {
        mapOf(
                AlertFailureCodes.NETWORK to AlertsProblem.NETWORK,
                AlertFailureCodes.NOT_FOUND to AlertsProblem.NOT_FOUND,
                AlertFailureCodes.STATUS_NOT_ACCEPTED to AlertsProblem.UNKNOWN,
            )
            .forEach { (code, problem) ->
                val model = model()
                repository.update = failure(code)

                model.updateStatus(AlertStatus.ATTENDED)

                assertEquals(code, AlertStatus.OPEN, content(model).alert.status)
                assertEquals(code, null, content(model).updating)
                assertEquals(code, AlertFeedback.Failed(problem), content(model).feedback)
            }
    }

    @Test
    fun losingAccessDuringUpdateClearsAlertContactAndNoteEditor() {
        listOf(
                AlertFailureCodes.ACCESS_DENIED,
                AlertFailureCodes.SESSION_REQUIRED,
                "SESSION_CHANGED",
                "CARE_RELATIONSHIP_REQUIRED",
            )
            .forEach { code ->
                val model = model()
                model.noteComposer.open()
                repository.update = failure(code)
                model.updateStatus(AlertStatus.ATTENDED)
                assertEquals(
                    AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED),
                    model.state.value,
                )
                assertEquals(
                    ContactUiState.Failed(AlertsProblem.ACCESS_DENIED),
                    model.contact.value,
                )
                assertFalse(model.noteComposer.state.value.open)
            }
    }

    @Test
    fun losingAccessWhileSavingANoteClearsProtectedDetail() {
        listOf(
                AlertFailureCodes.ACCESS_DENIED,
                AlertFailureCodes.SESSION_REQUIRED,
                "SESSION_CHANGED",
                "CARE_RELATIONSHIP_REQUIRED",
            )
            .forEach { code ->
                val model = model()
                notes.registered = failure(code)
                model.noteComposer.open()
                model.noteComposer.save("Seguimiento realizado")
                assertEquals(
                    AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED),
                    model.state.value,
                )
                assertEquals(
                    ContactUiState.Failed(AlertsProblem.ACCESS_DENIED),
                    model.contact.value,
                )
                assertFalse(model.noteComposer.state.value.open)
                assertFalse(model.noteComposer.state.value.saved)
            }
    }

    @Test
    fun returningToDetailAfterRevocationDiscardsProtectedEvidence() {
        val model = model()
        model.noteComposer.open()
        repository.detail = failure(AlertFailureCodes.ACCESS_DENIED)
        model.refresh()
        assertEquals(AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
        assertEquals(ContactUiState.Failed(AlertsProblem.ACCESS_DENIED), model.contact.value)
        assertFalse(model.noteComposer.state.value.open)
    }

    @Test
    fun resumeDoesNotInterruptAnAlertMutation() {
        val model = model()
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        model.updateStatus(AlertStatus.ATTENDED)
        val requests = repository.requests.toList()
        model.refresh()
        assertEquals(requests, repository.requests)
        gate.complete(Unit)
    }

    @Test
    fun resumeHistorySurvivesARecreatedRouteForTheSameViewModel() {
        val model = model()
        val initial = repository.requests.toList()
        model.onResume()
        assertEquals(initial, repository.requests)
        repository.detail = failure(AlertFailureCodes.ACCESS_DENIED)
        model.onResume()
        assertEquals(AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED), model.state.value)
    }
}
