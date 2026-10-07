package com.vitahealth.tata.monitoring.presentation.summary

import androidx.lifecycle.*
import com.vitahealth.tata.monitoring.application.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.domain.model.FamilySummary
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*

data class SummaryDialog(val title: String, val rows: List<String> = emptyList(), val loading: Boolean = false, val phone: String? = null)
data class FamilySummaryUiState(val loading: Boolean = true, val summary: FamilySummary? = null,
    val error: String? = null, val dialog: SummaryDialog? = null)

class FamilySummaryViewModel(private val caregiverId: String, private val olderAdultId: String,
    private val name: String, private val repository: FamilyMonitoringRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FamilySummaryUiState())
    val state = mutableState.asStateFlow()
    private var refreshJob: Job? = null
    private var dialogJob: Job? = null
    fun refresh() {
        refreshJob?.cancel()
        mutableState.value = mutableState.value.copy(loading = true, error = null)
        refreshJob = viewModelScope.launch {
            when (val result = repository.summary(caregiverId, olderAdultId, name, LocalDate.now(), ZoneId.systemDefault())) {
                is AppResult.Success -> mutableState.value = mutableState.value.copy(loading = false, summary = result.value)
                is AppResult.Failure -> mutableState.value = mutableState.value.copy(loading = false, summary = null, error = result.message)
            }
        }
    }
    fun dismissDialog() { dialogJob?.cancel(); mutableState.value = mutableState.value.copy(dialog = null) }
    fun history() = loadList("Historial de los últimos 7 días") { repository.history(caregiverId, olderAdultId) }
    fun notes() = loadList("Notas del cuidador") { repository.notes(caregiverId, olderAdultId) }
    fun contact() {
        dialogJob?.cancel()
        mutableState.value = mutableState.value.copy(dialog = SummaryDialog("Contactar", loading = true))
        dialogJob = viewModelScope.launch {
            val dialog = when (val result = repository.contact(caregiverId, olderAdultId)) {
                is AppResult.Success -> SummaryDialog("Contactar", listOf(result.value), phone = result.value)
                is AppResult.Failure -> SummaryDialog("Contactar", listOf(result.message))
            }
            mutableState.value = mutableState.value.copy(dialog = dialog)
        }
    }
    private fun loadList(title: String, load: suspend () -> AppResult<List<String>>) {
        dialogJob?.cancel()
        mutableState.value = mutableState.value.copy(dialog = SummaryDialog(title, loading = true))
        dialogJob = viewModelScope.launch {
            val rows = when (val result = load()) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> listOf(result.message)
            }
            mutableState.value = mutableState.value.copy(dialog = SummaryDialog(title, rows))
        }
    }
    class Factory(private val caregiverId: String, private val olderAdultId: String, private val name: String,
        private val repository: FamilyMonitoringRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FamilySummaryViewModel(caregiverId, olderAdultId, name, repository) as T
    }
}
