package com.vitahealth.tata.monitoring.presentation.alerts

import com.vitahealth.tata.monitoring.FakeAlertsRepository
import com.vitahealth.tata.monitoring.FakeNotesRepository
import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.handlers.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetOpenAlertsQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.handlers.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.failure
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The view models run on [Dispatchers.Unconfined], so each read finishes before the assertion. */
class AlertsViewModelTest {
    private val repository = FakeAlertsRepository()

    private fun listModel() = AlertsViewModel("caregiver", "adult", GetOpenAlertsQueryHandler(repository), Dispatchers.Unconfined)
    private fun detailModel() = AlertDetailViewModel(
        "caregiver", "adult", 1,
        GetAlertDetailQueryHandler(repository),
        UpdateAlertStatusCommandHandler(repository),
        RegisterFollowUpNoteCommandHandler(FakeNotesRepository()),
        Dispatchers.Unconfined,
    )

    @Test fun listStartsLoadingUntilTheReadFinishes() {
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }
        repository.list = AppResult.Success(listOf(alert()))
        val model = listModel()

        model.refresh()
        assertEquals(AlertsUiState.Loading, model.state.value)

        gate.complete(Unit)
        assertTrue(model.state.value is AlertsUiState.Content)
    }

    @Test fun listShowsTheAlerts() {
        repository.list = AppResult.Success(listOf(alert(id = 1), alert(id = 2, status = AlertStatus.ATTENDED)))
        val model = listModel()

        model.refresh()

        val state = model.state.value as AlertsUiState.Content
        assertEquals(listOf(1L, 2L), state.visibleAlerts.map { it.id })
        assertEquals(1, state.pendingCount)
    }

    @Test fun listWithoutAlertsIsEmpty() {
        repository.list = AppResult.Success(emptyList())
        val model = listModel()

        model.refresh()

        assertEquals(AlertsUiState.Empty, model.state.value)
    }

    @Test fun listExplainsEachFailure() {
        val expected = mapOf(
            AlertFailureCodes.NETWORK to AlertsProblem.NETWORK,
            AlertFailureCodes.ACCESS_DENIED to AlertsProblem.ACCESS_DENIED,
            AlertFailureCodes.NOT_FOUND to AlertsProblem.NOT_FOUND,
            AlertFailureCodes.REQUEST_FAILED to AlertsProblem.UNKNOWN,
            AlertFailureCodes.INVALID_RESPONSE to AlertsProblem.UNKNOWN,
        )
        expected.forEach { (code, problem) ->
            repository.list = failure(code)
            val model = listModel()

            model.refresh()

            assertEquals(code, AlertsUiState.Error(problem), model.state.value)
        }
    }

    @Test fun filtersPendingAndAttendedAlerts() {
        repository.list = AppResult.Success(listOf(alert(id = 1), alert(id = 2, status = AlertStatus.ATTENDED)))
        val model = listModel()
        model.refresh()

        model.selectFilter(AlertFilter.PENDING)
        assertEquals(listOf(1L), (model.state.value as AlertsUiState.Content).visibleAlerts.map { it.id })

        model.selectFilter(AlertFilter.ATTENDED)
        assertEquals(listOf(2L), (model.state.value as AlertsUiState.Content).visibleAlerts.map { it.id })
    }

    @Test fun refreshKeepsTheChosenFilterAndTheVisibleAlerts() {
        repository.list = AppResult.Success(listOf(alert(id = 1), alert(id = 2, status = AlertStatus.ATTENDED)))
        val model = listModel()
        model.refresh()
        model.selectFilter(AlertFilter.ATTENDED)
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        model.refresh()
        assertTrue(model.state.value is AlertsUiState.Content)

        gate.complete(Unit)
        assertEquals(AlertFilter.ATTENDED, (model.state.value as AlertsUiState.Content).filter)
    }

    @Test fun retryAfterAnErrorShowsTheAlerts() {
        repository.list = failure(AlertFailureCodes.NETWORK)
        val model = listModel()
        model.refresh()

        repository.list = AppResult.Success(listOf(alert()))
        model.refresh()

        assertTrue(model.state.value is AlertsUiState.Content)
    }

    @Test fun detailStartsLoading() {
        repository.gate = CompletableDeferred()

        assertEquals(AlertDetailUiState.Loading, detailModel().state.value)
    }

    @Test fun detailShowsTheAlert() {
        repository.detail = AppResult.Success(alert(id = 1))

        val state = detailModel().state.value

        assertEquals(1L, (state as AlertDetailUiState.Content).alert.id)
        assertEquals("detail:caregiver:adult:1", repository.requests.single())
    }

    @Test fun detailExplainsNotFoundAccessAndNetwork() {
        repository.detail = failure(AlertFailureCodes.NOT_FOUND)
        assertEquals(AlertDetailUiState.Error(AlertsProblem.NOT_FOUND), detailModel().state.value)

        repository.detail = failure(AlertFailureCodes.ACCESS_DENIED)
        assertEquals(AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED), detailModel().state.value)

        repository.detail = failure(AlertFailureCodes.NETWORK)
        assertEquals(AlertDetailUiState.Error(AlertsProblem.NETWORK), detailModel().state.value)
    }
}
