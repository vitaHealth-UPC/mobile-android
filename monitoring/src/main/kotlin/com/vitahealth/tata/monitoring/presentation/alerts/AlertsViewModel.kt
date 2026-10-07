package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.handlers.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.GetOpenAlertsQueryHandler
import com.vitahealth.tata.monitoring.application.handlers.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.handlers.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommand
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQuery
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.canMoveTo
import com.vitahealth.tata.monitoring.presentation.notes.NoteComposer
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** The route calls [refresh] on every resume, so the list follows alerts attended elsewhere. */
class AlertsViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val handler: GetOpenAlertsQueryHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {
    private val mutableState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val state: StateFlow<AlertsUiState> = mutableState.asStateFlow()
    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        val current = mutableState.value
        // A refresh over visible alerts keeps them on screen instead of flashing the loading state.
        if (current !is AlertsUiState.Content) mutableState.value = AlertsUiState.Loading
        job = viewModelScope.launch(context) {
            mutableState.value = when (val result = handler(GetOpenAlertsQuery(caregiverId, olderAdultId))) {
                is AppResult.Success -> when {
                    result.value.isEmpty() -> AlertsUiState.Empty
                    current is AlertsUiState.Content -> current.copy(alerts = result.value)
                    else -> AlertsUiState.Content(result.value)
                }
                is AppResult.Failure -> AlertsUiState.Error(problemOf(result.code))
            }
        }
    }

    fun selectFilter(filter: AlertFilter) {
        val current = mutableState.value
        if (current is AlertsUiState.Content) mutableState.value = current.copy(filter = filter)
    }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val handler: GetOpenAlertsQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlertsViewModel(caregiverId, olderAdultId, handler) as T
    }
}

class AlertDetailViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val alertId: Long,
    private val handler: GetAlertDetailQueryHandler,
    private val updateStatusHandler: UpdateAlertStatusCommandHandler,
    registerNoteHandler: RegisterFollowUpNoteCommandHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {
    private val mutableState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val state: StateFlow<AlertDetailUiState> = mutableState.asStateFlow()

    /** "Agregar nota de seguimiento": the backend links notes to the older adult, not to this alert. */
    val noteComposer = NoteComposer(caregiverId, olderAdultId, registerNoteHandler, viewModelScope, context)
    private var job: Job? = null

    init {
        load()
    }

    fun load() {
        job?.cancel()
        mutableState.value = AlertDetailUiState.Loading
        job = viewModelScope.launch(context) {
            mutableState.value = when (val result = handler(GetAlertDetailQuery(caregiverId, olderAdultId, alertId))) {
                is AppResult.Success -> AlertDetailUiState.Content(result.value)
                is AppResult.Failure -> AlertDetailUiState.Error(problemOf(result.code))
            }
        }
    }

    /** Attends or closes the alert; a request already in flight or a move the alert cannot make is ignored. */
    fun updateStatus(target: AlertStatus) {
        val current = mutableState.value as? AlertDetailUiState.Content ?: return
        if (current.updating != null || !current.alert.status.canMoveTo(target)) return
        mutableState.value = current.copy(updating = target, feedback = null)
        job = viewModelScope.launch(context) {
            val command = UpdateAlertStatusCommand(caregiverId, olderAdultId, alertId, target)
            mutableState.value = when (val result = updateStatusHandler(command)) {
                is AppResult.Success -> AlertDetailUiState.Content(result.value, feedback = AlertFeedback.StatusChanged(result.value.status))
                is AppResult.Failure -> afterFailedUpdate(current, problemOf(result.code))
            }
        }
    }

    /** On a 409 the alert moved elsewhere: show its current state instead of the stale one. */
    private suspend fun afterFailedUpdate(current: AlertDetailUiState.Content, problem: AlertsProblem): AlertDetailUiState {
        val failed = AlertFeedback.Failed(problem)
        if (problem != AlertsProblem.CONFLICT) return current.copy(updating = null, feedback = failed)
        return when (val fresh = handler(GetAlertDetailQuery(caregiverId, olderAdultId, alertId))) {
            is AppResult.Success -> AlertDetailUiState.Content(fresh.value, feedback = failed)
            is AppResult.Failure -> current.copy(updating = null, feedback = failed)
        }
    }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val alertId: Long,
        private val handler: GetAlertDetailQueryHandler,
        private val updateStatusHandler: UpdateAlertStatusCommandHandler,
        private val registerNoteHandler: RegisterFollowUpNoteCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlertDetailViewModel(caregiverId, olderAdultId, alertId, handler, updateStatusHandler, registerNoteHandler) as T
    }
}
