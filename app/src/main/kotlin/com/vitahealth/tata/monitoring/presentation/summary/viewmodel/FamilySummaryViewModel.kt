package com.vitahealth.tata.monitoring.presentation.summary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQueryHandler
import com.vitahealth.tata.monitoring.presentation.summary.mappers.toHistoryRow
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Coordinates summary refreshes and protected contact/history actions. */
class FamilySummaryViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val name: String,
    private val summaryHandler: GetFamilySummaryQueryHandler,
    private val historyHandler: GetFamilyHistoryQueryHandler,
    private val contactHandler: ContactOptionReader,
) : ViewModel() {
    private val mutableState = MutableStateFlow(FamilySummaryUiState())
    val state = mutableState.asStateFlow()
    private var refreshJob: Job? = null
    private var dialogJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        mutableState.value = mutableState.value.copy(loading = true, error = null, errorCode = null)
        refreshJob =
            viewModelScope.launch {
                when (
                    val result =
                        summaryHandler(
                            GetFamilySummaryQuery(
                                caregiverId,
                                olderAdultId,
                                name,
                                LocalDate.now(),
                                ZoneId.systemDefault(),
                            )
                        )
                ) {
                    is AppResult.Success -> {
                        currentCoroutineContext().ensureActive()
                        mutableState.value =
                            mutableState.value.copy(loading = false, summary = result.value)
                    }
                    is AppResult.Failure -> {
                        currentCoroutineContext().ensureActive()
                        if (clearProtectedState(result)) return@launch
                        mutableState.value =
                            mutableState.value.copy(
                                loading = false,
                                summary = null,
                                error = result.message,
                                errorCode = result.code,
                            )
                    }
                }
            }
    }

    fun dismissDialog() {
        dialogJob?.cancel()
        mutableState.value = mutableState.value.copy(dialog = null)
    }

    fun history() =
        loadList("Historial de los últimos 7 días") {
            when (val result = historyHandler(GetFamilyHistoryQuery(caregiverId, olderAdultId))) {
                is AppResult.Success -> AppResult.Success(result.value.map { it.toHistoryRow() })
                is AppResult.Failure -> result
            }
        }

    fun contact() {
        dialogJob?.cancel()
        mutableState.value =
            mutableState.value.copy(dialog = SummaryDialog("Contactar", loading = true))
        dialogJob =
            viewModelScope.launch {
                val result = contactHandler(GetContactOptionQuery(caregiverId, olderAdultId))
                currentCoroutineContext().ensureActive()
                if (result is AppResult.Failure && clearProtectedState(result)) return@launch
                val dialog =
                    when (result) {
                        is AppResult.Success ->
                            result.value.channel?.let {
                                SummaryDialog("Contactar", listOf(it.value), contact = it)
                            }
                                ?: SummaryDialog(
                                    "Contactar",
                                    listOf("No hay un canal de contacto registrado."),
                                )
                        is AppResult.Failure -> SummaryDialog("Contactar", listOf(result.message))
                    }
                currentCoroutineContext().ensureActive()
                mutableState.value = mutableState.value.copy(dialog = dialog)
            }
    }

    private fun loadList(title: String, load: suspend () -> AppResult<List<String>>) {
        dialogJob?.cancel()
        mutableState.value = mutableState.value.copy(dialog = SummaryDialog(title, loading = true))
        dialogJob =
            viewModelScope.launch {
                val result = load()
                currentCoroutineContext().ensureActive()
                if (result is AppResult.Failure && clearProtectedState(result)) return@launch
                val rows =
                    when (result) {
                        is AppResult.Success -> result.value
                        is AppResult.Failure -> listOf(result.message)
                    }
                currentCoroutineContext().ensureActive()
                mutableState.value = mutableState.value.copy(dialog = SummaryDialog(title, rows))
            }
    }

    /** Removes protected content and cancels sibling reads after access is lost. */
    private fun clearProtectedState(failure: AppResult.Failure): Boolean {
        if (
            failure.code !in
                setOf("SESSION_REQUIRED", "SESSION_CHANGED", "CARE_RELATIONSHIP_REQUIRED")
        ) {
            return false
        }
        refreshJob?.cancel()
        dialogJob?.cancel()
        mutableState.value =
            FamilySummaryUiState(loading = false, error = failure.message, errorCode = failure.code)
        return true
    }

    /** Creates the summary model for one caregiver and older adult pair. */
    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val name: String,
        private val summaryHandler: GetFamilySummaryQueryHandler,
        private val historyHandler: GetFamilyHistoryQueryHandler,
        private val contactHandler: ContactOptionReader,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FamilySummaryViewModel::class.java))
            return FamilySummaryViewModel(
                caregiverId,
                olderAdultId,
                name,
                summaryHandler,
                historyHandler,
                contactHandler,
            )
                as T
        }
    }
}
