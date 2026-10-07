package com.vitahealth.tata.identity.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.handlers.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.handlers.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanSubscriptionViewModel(
    private val accountId: String,
    private val getSubscription: GetCurrentSubscriptionQueryHandler,
    private val listPlans: ListAvailablePlansQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(PlanSubscriptionUiState())
    val state: StateFlow<PlanSubscriptionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val subscription = getSubscription(GetCurrentSubscriptionQuery(accountId))
            if (subscription is AppResult.Failure) {
                _state.update { it.copy(isLoading = false, message = errorMessage(subscription)) }
                return@launch
            }
            val plans = listPlans()
            if (plans is AppResult.Failure) {
                _state.update { it.copy(isLoading = false, message = errorMessage(plans)) }
                return@launch
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    subscription = (subscription as AppResult.Success).value,
                    plans = (plans as AppResult.Success).value,
                )
            }
        }
    }

    private fun errorMessage(failure: AppResult.Failure): PlanMessage =
        when (failure.code) {
            "NETWORK_UNAVAILABLE" -> PlanMessage.ErrorOffline
            "ACCOUNT_NOT_FOUND", "ACCOUNT_NOT_ACTIVE", "INVALID_ACCOUNT_REFERENCE" -> PlanMessage.ErrorAccount
            "UNAUTHENTICATED" -> PlanMessage.ErrorSession
            else -> PlanMessage.ErrorGeneric
        }

    class Factory(
        private val accountId: String,
        private val getSubscription: GetCurrentSubscriptionQueryHandler,
        private val listPlans: ListAvailablePlansQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlanSubscriptionViewModel(accountId, getSubscription, listPlans) as T
    }
}
