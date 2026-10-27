package com.vitahealth.tata.identity.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.queries.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Public plan catalog only. Selecting here never creates a paid subscription. */
data class LandingPlansUiState(
    val loading: Boolean = true,
    val plans: List<Plan> = emptyList(),
    val error: Boolean = false,
)

class LandingPlansViewModel(
    private val listPlans: ListAvailablePlansQueryHandler,
) : ViewModel() {
    private val mutable = MutableStateFlow(LandingPlansUiState())
    val state: StateFlow<LandingPlansUiState> = mutable.asStateFlow()
    private var readJob: Job? = null

    init { refresh() }

    fun refresh() {
        if (readJob?.isActive == true) return
        mutable.value = LandingPlansUiState()
        readJob = viewModelScope.launch {
            val result = listPlans()
            currentCoroutineContext().ensureActive()
            mutable.value = when (result) {
                is AppResult.Success ->
                    if (result.value.isEmpty()) LandingPlansUiState(loading = false, error = true)
                    else LandingPlansUiState(loading = false, plans = result.value)
                is AppResult.Failure -> LandingPlansUiState(loading = false, error = true)
            }
        }
    }

    class Factory(private val query: ListAvailablePlansQueryHandler) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LandingPlansViewModel(query) as T
    }
}
