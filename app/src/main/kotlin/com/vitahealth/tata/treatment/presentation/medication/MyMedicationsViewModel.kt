package com.vitahealth.tata.treatment.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.handlers.GetMyMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

data class MyMedicationsUiState(
    val medications: List<MyMedication> = emptyList(),
    val nextDose: MedicationNextDose? = null,
    val loading: Boolean = true,
    val errorCode: String? = null,
    val history: Boolean = false,
)

class MyMedicationsViewModel(
    private val query: GetMyMedicationsQueryHandler,
    private val nextDoseQuery: suspend () -> MedicationNextDose?,
) : ViewModel() {
    private val mutable = MutableStateFlow(MyMedicationsUiState())
    val state = mutable.asStateFlow()

    private var loadingJob: Job? = null

    init { refresh() }

    fun selectHistory(value: Boolean) { mutable.update { it.copy(history = value) } }

    fun refresh() {
        if (loadingJob?.isActive == true) return
        mutable.update { it.copy(loading = true, errorCode = null) }
        loadingJob = viewModelScope.launch {
            when(val result = query()) {
                is AppResult.Success -> {
                    val nextDose = nextDoseQuery()
                    mutable.update { it.copy(medications = result.value, nextDose = nextDose, loading = false) }
                }
                is AppResult.Failure -> mutable.update { it.copy(loading = false, errorCode = result.code ?: "REQUEST_FAILED") }
            }
        }
    }

    class Factory(
        private val query: GetMyMedicationsQueryHandler,
        private val nextDoseQuery: suspend () -> MedicationNextDose?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MyMedicationsViewModel(query, nextDoseQuery) as T
    }
}
