package com.vitahealth.tata.intake.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.queries.GetDailyDoseProgressQuery
import com.vitahealth.tata.intake.application.queries.GetDailyDoseProgressQueryHandler
import com.vitahealth.tata.intake.application.queries.GetNextDoseQuery
import com.vitahealth.tata.intake.application.queries.GetNextDoseQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NextDoseHomeViewModel(
    private val olderAdultId: String,
    private val olderAdultName: String,
    private val handler: GetNextDoseQueryHandler,
    private val progressHandler: GetDailyDoseProgressQueryHandler? = null,
) : ViewModel() {
    private val _state = MutableStateFlow<NextDoseHomeUiState>(NextDoseHomeUiState.Loading)
    val state: StateFlow<NextDoseHomeUiState> = _state.asStateFlow()

    private var request: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        request?.cancel()
        _state.value = NextDoseHomeUiState.Loading
        request =
            viewModelScope.launch {
                val zone = ZoneId.systemDefault()
                val day = LocalDate.now(zone)
                val progress =
                    (progressHandler?.invoke(GetDailyDoseProgressQuery(olderAdultId, day, zone))
                            as? AppResult.Success)
                        ?.value
                when (val result = handler(GetNextDoseQuery(olderAdultId))) {
                    is AppResult.Success -> {
                        _state.value =
                            result.value?.let {
                                NextDoseHomeUiState.NextDoseAvailable(
                                    dose = it,
                                    olderAdultName = olderAdultName,
                                    progress = progress,
                                )
                            } ?: NextDoseHomeUiState.NoNextDose(olderAdultName, progress)
                    }
                    is AppResult.Failure -> {
                        _state.value =
                            NextDoseHomeUiState.Error(
                                olderAdultName = olderAdultName,
                                message = result.message,
                            )
                    }
                }
            }
    }

    class Factory(
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val handler: GetNextDoseQueryHandler,
        private val progressHandler: GetDailyDoseProgressQueryHandler? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NextDoseHomeViewModel(
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                handler = handler,
                progressHandler = progressHandler,
            )
                as T
    }
}
