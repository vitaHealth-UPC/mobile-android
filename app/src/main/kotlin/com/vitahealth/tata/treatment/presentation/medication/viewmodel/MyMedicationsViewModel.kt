package com.vitahealth.tata.treatment.presentation.medication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsOverviewReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the adult catalog and its next intake without treating failed lookups as an empty agenda.
 */
class MyMedicationsViewModel(private val reader: MyMedicationsOverviewReader) : ViewModel() {
    private val mutable = MutableStateFlow(MyMedicationsUiState())
    val state = mutable.asStateFlow()

    private var loadingJob: Job? = null

    init {
        refresh()
    }

    fun selectHistory(value: Boolean) {
        mutable.update { it.copy(history = value) }
    }

    fun refresh() {
        if (loadingJob?.isActive == true) return
        mutable.update { it.copy(loading = true, errorCode = null) }
        loadingJob =
            viewModelScope.launch {
                val result = reader.read()
                currentCoroutineContext().ensureActive()
                when (result) {
                    is AppResult.Success ->
                        mutable.update {
                            it.copy(
                                medications = result.value.medications,
                                nextDose = result.value.nextDose,
                                loading = false,
                            )
                        }
                    is AppResult.Failure ->
                        mutable.update {
                            it.copy(
                                loading = false,
                                nextDose = null,
                                errorCode = result.code ?: "REQUEST_FAILED",
                            )
                        }
                }
            }
    }

    /** Supplies both read operations and rejects unrelated ViewModel requests. */
    class Factory(private val reader: MyMedicationsOverviewReader) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MyMedicationsViewModel::class.java)) {
                "Unsupported ViewModel: ${modelClass.name}"
            }
            return MyMedicationsViewModel(reader) as T
        }
    }
}
