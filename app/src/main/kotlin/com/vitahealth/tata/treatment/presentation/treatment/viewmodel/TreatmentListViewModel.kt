package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQuery
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQueryHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the treatment catalog while retaining the last successful list during refresh errors. */
class TreatmentListViewModel(
    private val caregiverId: String,
    private val olderAdultId: String,
    private val query: ListTreatmentsQueryHandler,
) : ViewModel() {
    private val mutable = MutableStateFlow(TreatmentListState())
    val state = mutable.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        mutable.update { it.copy(loading = true, error = null) }
        refreshJob =
            viewModelScope.launch {
                val result = query(ListTreatmentsQuery(caregiverId, olderAdultId))
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success -> mutable.value = TreatmentListState(result.value, false)
                    is AppResult.Failure ->
                        mutable.update { it.copy(loading = false, error = result.code) }
                }
            }
    }

    /** Supplies the caregiver, adult and query dependencies for this screen. */
    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val query: ListTreatmentsQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TreatmentListViewModel::class.java)) {
                "Unsupported ViewModel: ${modelClass.name}"
            }
            return TreatmentListViewModel(caregiverId, olderAdultId, query) as T
        }
    }
}
