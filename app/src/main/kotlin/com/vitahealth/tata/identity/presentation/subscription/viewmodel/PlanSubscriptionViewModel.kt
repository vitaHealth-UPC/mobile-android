package com.vitahealth.tata.identity.presentation.subscription.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommand
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommandHandler
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQueryHandler
import com.vitahealth.tata.identity.application.queries.ListAvailablePlansQueryHandler
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates plan loading and explicit account subscription changes. */
class PlanSubscriptionViewModel(
    private val accountId: String,
    private val getSubscription: GetCurrentSubscriptionQueryHandler,
    private val listPlans: ListAvailablePlansQueryHandler,
    private val changeSubscription: ChangeSubscriptionCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(PlanSubscriptionUiState())
    val state: StateFlow<PlanSubscriptionUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Supersedes older reads and leaves an active plan change in control of its result. */
    fun load() {
        if (_state.value.isChanging) return
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, message = null) }
        loadJob =
            viewModelScope.launch {
                val subscription = getSubscription(GetCurrentSubscriptionQuery(accountId))
                currentCoroutineContext().ensureActive()
                if (subscription is AppResult.Failure) {
                    if (clearAccessFailure(subscription)) return@launch
                    _state.update { it.withoutUnverifiedPlan(errorMessage(subscription)) }
                    return@launch
                }
                val plans = listPlans()
                currentCoroutineContext().ensureActive()
                if (plans is AppResult.Failure) {
                    if (clearAccessFailure(plans)) return@launch
                    _state.update { it.withoutUnverifiedPlan(errorMessage(plans)) }
                    return@launch
                }
                val currentSubscription = (subscription as AppResult.Success).value
                val availablePlans = (plans as AppResult.Success).value
                _state.update { current ->
                    val selected = current.selectedPlan?.takeIf { candidate ->
                        availablePlans.any { it == candidate } &&
                            !currentSubscription.isOn(candidate)
                    }
                    current.copy(
                        isLoading = false,
                        subscription = currentSubscription,
                        plans = availablePlans,
                        selectedPlan = selected,
                        confirming = current.confirming?.takeIf { it == selected },
                        message = null,
                    )
                }
            }
    }

    fun onSelectPlan(plan: Plan) {
        val current = _state.value
        val subscription = current.subscription ?: return
        val available = current.plans.firstOrNull { it == plan } ?: return
        if (
            current.isLoading || current.isChanging || current.message != null ||
                subscription.isOn(available)
        ) return
        _state.update {
            // Tapping the selected plan again clears the choice.
            it.copy(
                selectedPlan = if (it.selectedPlan?.code == plan.code) null else plan,
                changeMessage = null,
            )
        }
    }

    fun onChangeRequest() {
        val current = _state.value
        val selected = current.selectedPlan ?: return
        if (
            current.isLoading || current.isChanging || current.message != null ||
                current.subscription == null || current.subscription.isOn(selected) ||
                current.plans.none { it == selected }
        ) return
        _state.update { it.copy(confirming = selected, changeMessage = null) }
    }

    fun onChangeCancel() = _state.update { it.copy(confirming = null) }

    fun onChangeConfirm() {
        val current = _state.value
        val target = current.confirming ?: return
        if (current.isChanging || current.message != null ||
            current.subscription == null || current.plans.none { it == target }) return
        loadJob?.cancel()

        _state.update {
            it.copy(isLoading = false, isChanging = true, confirming = null, changeMessage = null)
        }
        viewModelScope.launch {
            val result = changeSubscription(ChangeSubscriptionCommand(accountId, target.code))
            currentCoroutineContext().ensureActive()
            if (result is AppResult.Failure && clearAccessFailure(result)) return@launch
            when (result) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(
                            isChanging = false,
                            subscription = result.value,
                            selectedPlan = null,
                            changeMessage = PlanChangeMessage.Updated,
                        )
                    }

                is AppResult.Failure ->
                    _state.update {
                        it.copy(isChanging = false, changeMessage = changeError(result))
                    }
            }
        }
    }

    /** A failed reload must not leave stale plan choices available for mutation. */
    private fun PlanSubscriptionUiState.withoutUnverifiedPlan(reason: PlanMessage) =
        copy(
            isLoading = false,
            subscription = null,
            plans = emptyList(),
            selectedPlan = null,
            confirming = null,
            isChanging = false,
            changeMessage = null,
            message = reason,
        )

    /** A denied or superseded session cannot retain the previous account's plan or selection. */
    private fun clearAccessFailure(failure: AppResult.Failure): Boolean {
        val message =
            when (failure.code) {
                "UNAUTHENTICATED",
                "SESSION_CHANGED",
                "ACCOUNT_ACCESS_DENIED" -> PlanMessage.ErrorSession
                "ACCOUNT_NOT_ACTIVE",
                "ACCOUNT_NOT_FOUND" -> PlanMessage.ErrorAccount
                else -> return false
            }
        _state.value = PlanSubscriptionUiState(isLoading = false, message = message)
        return true
    }

    private fun changeError(failure: AppResult.Failure): PlanChangeMessage =
        when (failure.code) {
            "PLAN_NOT_FOUND",
            "INVALID_PLAN_REFERENCE",
            "VALIDATION_ERROR" -> PlanChangeMessage.ErrorPlanUnavailable
            "ACCOUNT_NOT_FOUND",
            "ACCOUNT_NOT_ACTIVE",
            "INVALID_ACCOUNT_REFERENCE" -> PlanChangeMessage.ErrorAccount
            "UNAUTHENTICATED" -> PlanChangeMessage.ErrorSession
            "NETWORK_UNAVAILABLE" -> PlanChangeMessage.ErrorOffline
            else -> PlanChangeMessage.ErrorGeneric
        }

    private fun errorMessage(failure: AppResult.Failure): PlanMessage =
        when (failure.code) {
            "NETWORK_UNAVAILABLE" -> PlanMessage.ErrorOffline
            "ACCOUNT_NOT_FOUND",
            "ACCOUNT_NOT_ACTIVE",
            "INVALID_ACCOUNT_REFERENCE" -> PlanMessage.ErrorAccount
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
            PlanSubscriptionViewModel(accountId, getSubscription, listPlans, changeSubscription)
                as T
    }
}
