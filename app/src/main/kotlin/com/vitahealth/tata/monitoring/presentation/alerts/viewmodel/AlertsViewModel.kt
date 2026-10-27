package com.vitahealth.tata.monitoring.presentation.alerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.queries.GetAlertTimelineQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The route calls [refresh] on every resume, so the list follows alerts attended elsewhere. */
class AlertsViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val handler: GetOpenAlertsQueryHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val timeline: GetAlertTimelineQueryHandler? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val mutableState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val state: StateFlow<AlertsUiState> = mutableState.asStateFlow()
    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        val current = mutableState.value
        // A refresh over visible alerts keeps them on screen instead of flashing the loading state.
        if (current !is AlertsUiState.Content) mutableState.value = AlertsUiState.Loading
        job =
            viewModelScope.launch(context) {
                mutableState.value =
                    when (
                        val result =
                            loadTimeline().also { currentCoroutineContext().ensureActive() }
                    ) {
                        is AppResult.Success ->
                            when {
                                result.value.alerts.isEmpty() &&
                                    result.value.visibleDoses.isEmpty() -> AlertsUiState.Empty
                                mutableState.value is AlertsUiState.Content ->
                                    (mutableState.value as AlertsUiState.Content).copy(
                                        alerts = result.value.alerts,
                                        doses = result.value.doses,
                                        olderAdultName = result.value.olderAdultName,
                                        day = result.value.day,
                                        asOf = result.value.asOf,
                                    )
                                else -> result.value
                            }
                        is AppResult.Failure -> AlertsUiState.Error(problemOf(result.code))
                    }
            }
    }

    private suspend fun loadTimeline(): AppResult<AlertsUiState.Content> {
        val query = timeline
        if (query != null) {
            return when (
                val result = query(caregiverId, olderAdultId, LocalDate.now(clock), clock.zone)
            ) {
                is AppResult.Success ->
                    AppResult.Success(
                        AlertsUiState.Content(
                            alerts = result.value.alerts,
                            doses = result.value.doses,
                            olderAdultName = result.value.olderAdultName,
                            day = result.value.day,
                            asOf = clock.instant(),
                        )
                    )
                is AppResult.Failure -> result
            }
        }
        return when (val result = handler(GetOpenAlertsQuery(caregiverId, olderAdultId))) {
            is AppResult.Success ->
                AppResult.Success(
                    AlertsUiState.Content(
                        result.value,
                        day = LocalDate.now(clock),
                        asOf = clock.instant(),
                    )
                )
            is AppResult.Failure -> result
        }
    }

    /** Advances presentation time without rereading unchanged protected data every minute. */
    fun updateTime(now: Instant = clock.instant()) {
        val current = mutableState.value as? AlertsUiState.Content ?: return
        if (now.atZone(clock.zone).toLocalDate() != current.day) {
            refresh()
        } else {
            mutableState.value = current.copy(asOf = now)
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
        private val timeline: GetAlertTimelineQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlertsViewModel(caregiverId, olderAdultId, handler, timeline = timeline) as T
    }
}
