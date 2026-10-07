package com.vitahealth.tata.identity.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommand
import com.vitahealth.tata.identity.application.handlers.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.handlers.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.handlers.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.identity.domain.model.Plan
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
    private val changeSubscription: ChangeSubscriptionCommandHandler,
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

    fun onSelectPlan(plan: Plan) {
        val current = _state.value
        val subscription = current.subscription ?: return
        if (current.isChanging || subscription.isOn(plan)) return
        _state.update {
            // Tapping the selected plan again clears the choice.
            it.copy(selectedPlan = if (it.selectedPlan?.code == plan.code) null else plan, changeMessage = null)
        }
    }

    fun onChangeRequest() {
        val current = _state.value
        val selected = current.selectedPlan ?: return
        if (current.isChanging) return
        _state.update { it.copy(confirming = selected, changeMessage = null) }
    }

    fun onChangeCancel() = _state.update { it.copy(confirming = null) }

    fun onChangeConfirm() {
        val current = _state.value
        val target = current.confirming ?: return
        if (current.isChanging) return

        _state.update { it.copy(isChanging = true, confirming = null, changeMessage = null) }
        viewModelScope.launch {
            when (val result = changeSubscription(ChangeSubscriptionCommand(accountId, target.code))) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isChanging = false,
                        subscription = result.value,
                        selectedPlan = null,
                        changeMessage = PlanChangeMessage.Updated,
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isChanging = false, changeMessage = changeError(result))
                }
            }
        }
    }

    private fun changeError(failure: AppResult.Failure): PlanChangeMessage =
        when (failure.code) {
            "PLAN_NOT_FOUND", "INVALID_PLAN_REFERENCE", "VALIDATION_ERROR" -> PlanChangeMessage.ErrorPlanUnavailable
            "ACCOUNT_NOT_FOUND", "ACCOUNT_NOT_ACTIVE", "INVALID_ACCOUNT_REFERENCE" -> PlanChangeMessage.ErrorAccount
            "UNAUTHENTICATED" -> PlanChangeMessage.ErrorSession
            "NETWORK_UNAVAILABLE" -> PlanChangeMessage.ErrorOffline
            else -> PlanChangeMessage.ErrorGeneric
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
        private val changeSubscription: ChangeSubscriptionCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlanSubscriptionViewModel(accountId, getSubscription, listPlans, changeSubscription) as T
    }
}
