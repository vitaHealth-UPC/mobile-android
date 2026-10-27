package com.vitahealth.tata.carelink.presentation.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsentWithdrawalUiState(
    val confirming: Boolean = false,
    val busy: Boolean = false,
    val withdrawn: Boolean = false,
    val errorCode: String? = null,
)

class ConsentWithdrawalViewModel(
    private val adultId: String,
    private val withdrawal: ConsentWithdrawal,
) : ViewModel() {
    private val mutable = MutableStateFlow(ConsentWithdrawalUiState())
    val state = mutable.asStateFlow()

    fun request() {
        if (!state.value.busy && !state.value.withdrawn)
            mutable.value = ConsentWithdrawalUiState(confirming = true)
    }

    fun cancel() {
        if (!state.value.busy) mutable.value = ConsentWithdrawalUiState()
    }

    fun confirm() {
        if (!state.value.confirming || state.value.busy || state.value.withdrawn) return
        mutable.value = state.value.copy(busy = true, errorCode = null)
        viewModelScope.launch {
            val result = withdrawal.withdraw(adultId)
            currentCoroutineContext().ensureActive()
            mutable.value =
                when (result) {
                    is AppResult.Success -> ConsentWithdrawalUiState(withdrawn = true)
                    is AppResult.Failure ->
                        ConsentWithdrawalUiState(
                            confirming = true,
                            errorCode = result.code ?: "REQUEST_FAILED",
                        )
                }
        }
    }

    class Factory(private val adultId: String, private val withdrawal: ConsentWithdrawal) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ConsentWithdrawalViewModel::class.java))
            return ConsentWithdrawalViewModel(adultId, withdrawal) as T
        }
    }
}
