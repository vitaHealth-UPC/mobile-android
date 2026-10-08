package com.vitahealth.tata.monitoring.presentation.summary

import androidx.lifecycle.*
import com.vitahealth.tata.monitoring.application.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.handlers.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.domain.model.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.FamilySummary
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*

data class SummaryDialog(val title: String, val rows: List<String> = emptyList(), val loading: Boolean = false, val contact: ContactChannel? = null)
data class FamilySummaryUiState(val loading: Boolean = true, val summary: FamilySummary? = null,
    val error: String? = null, val dialog: SummaryDialog? = null, val errorCode: String? = null)

class FamilySummaryViewModel(private val caregiverId: String, private val olderAdultId: String,
    private val name: String, private val repository: FamilyMonitoringRepository,
    private val contactHandler: GetContactOptionQueryHandler) : ViewModel() {
    private val mutableState = MutableStateFlow(FamilySummaryUiState())
    val state = mutableState.asStateFlow()
    private var refreshJob: Job? = null
    private var dialogJob: Job? = null
    fun refresh() {
        refreshJob?.cancel()
        mutableState.value = mutableState.value.copy(loading = true, error = null, errorCode = null)
        refreshJob = viewModelScope.launch {
            when (val result = repository.summary(caregiverId, olderAdultId, name, LocalDate.now(), ZoneId.systemDefault())) {
                is AppResult.Success -> mutableState.value = mutableState.value.copy(loading = false, summary = result.value)
                is AppResult.Failure -> mutableState.value = mutableState.value.copy(loading = false, summary = null, error = result.message, errorCode = result.code)
            }
        }
    }
    fun dismissDialog() { dialogJob?.cancel(); mutableState.value = mutableState.value.copy(dialog = null) }
    fun history() = loadList("Historial de los últimos 7 días") { repository.history(caregiverId, olderAdultId) }
    fun contact() {
        dialogJob?.cancel()
        mutableState.value = mutableState.value.copy(dialog = SummaryDialog("Contactar", loading = true))
        dialogJob = viewModelScope.launch {
            val dialog = when (val result = contactHandler(GetContactOptionQuery(caregiverId, olderAdultId))) {
                is AppResult.Success -> result.value.channel?.let { SummaryDialog("Contactar", listOf(it.value), contact = it) }
                    ?: SummaryDialog("Contactar", listOf("No hay un canal de contacto registrado."))
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
        private val repository: FamilyMonitoringRepository,
        private val contactHandler: GetContactOptionQueryHandler) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FamilySummaryViewModel(caregiverId, olderAdultId, name, repository, contactHandler) as T
    }
}
