package com.vitahealth.tata.monitoring.presentation.alerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommand
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.ports.AlertHistoryReader
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.ports.MonitoredDoseReader
import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQuery
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.canMoveTo
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NoteComposer
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads one alert and serializes its status mutations against refreshes. */
class AlertDetailViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val alertId: Long,
    private val handler: GetAlertDetailQueryHandler,
    private val updateStatusHandler: UpdateAlertStatusCommandHandler,
    registerNoteHandler: RegisterFollowUpNoteCommandHandler,
    private val contactHandler: ContactOptionReader,
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val personReader: MonitoredPersonReader? = null,
    private val doseReader: MonitoredDoseReader? = null,
    private val historyReader: AlertHistoryReader? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val state: StateFlow<AlertDetailUiState> = mutableState.asStateFlow()

    /**
     * "Agregar nota de seguimiento": the backend links notes to the older adult, not to this alert.
     */
    val noteComposer =
        NoteComposer(
            caregiverId,
            olderAdultId,
            registerNoteHandler,
            viewModelScope,
            context,
            onAccessDenied = { mutableState.value = denyAccess() },
        )
    private var job: Job? = null

    private val mutableContact = MutableStateFlow<ContactUiState>(ContactUiState.Loading)
    val contact: StateFlow<ContactUiState> = mutableContact.asStateFlow()
    private var contactJob: Job? = null

    private val mutablePerson = MutableStateFlow<MonitoredPerson?>(null)
    val person: StateFlow<MonitoredPerson?> = mutablePerson.asStateFlow()
    private var personJob: Job? = null

    private val mutableHistory = MutableStateFlow<List<RecordedAlertEvent>?>(null)
    val history: StateFlow<List<RecordedAlertEvent>?> = mutableHistory.asStateFlow()
    private var historyJob: Job? = null

    init {
        load()
        loadContact()
        loadPerson()
        loadHistory()
    }

    private var initiallyResumed = false

    /** Keeps foreground history with this detail even when its Compose route is recreated. */
    fun onResume() {
        if (initiallyResumed) refresh() else initiallyResumed = true
    }

    /** Refreshes protected evidence after returning from contact apps or the background. */
    fun refresh() {
        if (
            noteComposer.state.value.saving ||
                (mutableState.value as? AlertDetailUiState.Content)?.updating != null
        )
            return
        load()
        loadContact()
        loadPerson()
        loadHistory()
    }

    private fun loadHistory() {
        val reader = historyReader ?: return
        if (
            (mutableState.value as? AlertDetailUiState.Error)?.problem ==
                AlertsProblem.ACCESS_DENIED
        )
            return
        historyJob?.cancel()
        mutableHistory.value = null
        historyJob =
            viewModelScope.launch(context) {
                val result = reader.read(caregiverId, olderAdultId, alertId)
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> mutableHistory.value = result.value
                    is AppResult.Failure ->
                        if (problemOf(result.code) == AlertsProblem.ACCESS_DENIED)
                            mutableState.value = denyAccess()
                }
            }
    }

    private fun loadPerson() {
        val reader = personReader ?: return
        if (
            (mutableState.value as? AlertDetailUiState.Error)?.problem ==
                AlertsProblem.ACCESS_DENIED
        )
            return
        personJob?.cancel()
        mutablePerson.value = null
        personJob =
            viewModelScope.launch(context) {
                val result = reader.read(caregiverId, olderAdultId)
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> mutablePerson.value = result.value
                    is AppResult.Failure ->
                        if (problemOf(result.code) == AlertsProblem.ACCESS_DENIED)
                            mutableState.value = denyAccess()
                }
            }
    }

    /** Read beside the alert, so a slow contact lookup never delays the alert itself. */
    fun loadContact() {
        if (
            (mutableState.value as? AlertDetailUiState.Error)?.problem ==
                AlertsProblem.ACCESS_DENIED
        )
            return
        contactJob?.cancel()
        mutableContact.value = ContactUiState.Loading
        contactJob =
            viewModelScope.launch(context) {
                mutableContact.value =
                    when (
                        val result =
                            contactHandler(GetContactOptionQuery(caregiverId, olderAdultId)).also {
                                currentCoroutineContext().ensureActive()
                            }
                    ) {
                        is AppResult.Success -> ContactUiState.Ready(result.value)
                        is AppResult.Failure -> {
                            val problem = problemOf(result.code)
                            if (problem == AlertsProblem.ACCESS_DENIED)
                                mutableState.value = denyAccess()
                            ContactUiState.Failed(problem)
                        }
                    }
            }
    }

    fun load() {
        if ((mutableState.value as? AlertDetailUiState.Content)?.updating != null) return
        job?.cancel()
        mutableState.value = AlertDetailUiState.Loading
        job =
            viewModelScope.launch(context) {
                mutableState.value =
                    when (
                        val result =
                            handler(GetAlertDetailQuery(caregiverId, olderAdultId, alertId)).also {
                                currentCoroutineContext().ensureActive()
                            }
                    ) {
                        is AppResult.Success -> {
                            val doseResult =
                                doseReader?.read(caregiverId, olderAdultId, result.value.intakeId)
                            currentCoroutineContext().ensureActive()
                            if (
                                doseResult is AppResult.Failure &&
                                    problemOf(doseResult.code) == AlertsProblem.ACCESS_DENIED
                            )
                                denyAccess()
                            else
                                AlertDetailUiState.Content(
                                    result.value,
                                    dose = (doseResult as? AppResult.Success)?.value,
                                )
                        }
                        is AppResult.Failure -> {
                            val problem = problemOf(result.code)
                            if (problem == AlertsProblem.ACCESS_DENIED) denyAccess()
                            else AlertDetailUiState.Error(problem)
                        }
                    }
            }
    }

    /**
     * Attends or closes the alert; a request already in flight or a move the alert cannot make is
     * ignored.
     */
    fun updateStatus(target: AlertStatus) {
        val current = mutableState.value as? AlertDetailUiState.Content ?: return
        if (current.updating != null || !current.alert.status.canMoveTo(target)) return
        mutableState.value = current.copy(updating = target, feedback = null)
        job =
            viewModelScope.launch(context) {
                val command = UpdateAlertStatusCommand(caregiverId, olderAdultId, alertId, target)
                mutableState.value =
                    when (
                        val result =
                            updateStatusHandler(command).also {
                                currentCoroutineContext().ensureActive()
                            }
                    ) {
                        is AppResult.Success ->
                            AlertDetailUiState.Content(
                                result.value,
                                feedback = AlertFeedback.StatusChanged(result.value.status),
                                dose = current.dose,
                            )
                        is AppResult.Failure -> afterFailedUpdate(current, problemOf(result.code))
                    }
                if (mutableState.value is AlertDetailUiState.Content) loadHistory()
            }
    }

    /** On a 409 the alert moved elsewhere: show its current state instead of the stale one. */
    private suspend fun afterFailedUpdate(
        current: AlertDetailUiState.Content,
        problem: AlertsProblem,
    ): AlertDetailUiState {
        if (problem == AlertsProblem.ACCESS_DENIED) return denyAccess()
        val failed = AlertFeedback.Failed(problem)
        if (problem != AlertsProblem.CONFLICT)
            return current.copy(updating = null, feedback = failed)
        return when (
            val fresh =
                handler(GetAlertDetailQuery(caregiverId, olderAdultId, alertId)).also {
                    currentCoroutineContext().ensureActive()
                }
        ) {
            is AppResult.Success ->
                AlertDetailUiState.Content(
                    fresh.value,
                    feedback = failed,
                    dose = current.dose?.takeIf { it.id == fresh.value.intakeId },
                )
            is AppResult.Failure ->
                if (problemOf(fresh.code) == AlertsProblem.ACCESS_DENIED) denyAccess()
                else current.copy(updating = null, feedback = failed)
        }
    }

    /** Access failure invalidates every protected subflow of this detail. */
    private fun denyAccess(): AlertDetailUiState.Error {
        job?.cancel()
        contactJob?.cancel()
        personJob?.cancel()
        historyJob?.cancel()
        mutableHistory.value = null
        mutablePerson.value = null
        mutableContact.value = ContactUiState.Failed(AlertsProblem.ACCESS_DENIED)
        noteComposer.clear()
        return AlertDetailUiState.Error(AlertsProblem.ACCESS_DENIED)
    }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val alertId: Long,
        private val handler: GetAlertDetailQueryHandler,
        private val updateStatusHandler: UpdateAlertStatusCommandHandler,
        private val registerNoteHandler: RegisterFollowUpNoteCommandHandler,
        private val contactHandler: ContactOptionReader,
        private val personReader: MonitoredPersonReader? = null,
        private val doseReader: MonitoredDoseReader? = null,
        private val historyReader: AlertHistoryReader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlertDetailViewModel(
                caregiverId,
                olderAdultId,
                alertId,
                handler,
                updateStatusHandler,
                registerNoteHandler,
                contactHandler,
                personReader = personReader,
                doseReader = doseReader,
                historyReader = historyReader,
            )
                as T
    }
}
