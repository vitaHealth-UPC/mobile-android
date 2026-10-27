package com.vitahealth.tata.inventory.presentation.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQuery
import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQueryHandler
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommand
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommandHandler
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommand
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Coordinates stock queries and one inventory mutation at a time for the selected medication. */
class InventoryViewModel(
    private val medicationId: String,
    private val medicationName: String,
    private val unit: String,
    private val getStockHandler: GetInventoryStockQueryHandler,
    private val registerInitialHandler: RegisterInitialInventoryCommandHandler,
    private val registerReplenishmentHandler: RegisterReplenishmentCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val state: StateFlow<InventoryUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    // --- Initial inventory (US-40) ---

    fun onInitialQuantityChange(value: String) = updateNotInitialized {
        copy(initialQuantityInput = value, formErrorCode = null)
    }

    fun onThresholdChange(value: String) = updateNotInitialized {
        copy(thresholdInput = value, formErrorCode = null)
    }

    fun defineInitialInventory() {
        val current = _state.value as? InventoryUiState.NotInitialized ?: return
        if (current.submitting) return

        val quantity = current.initialQuantityInput.trim().toIntOrNull()
        if (quantity == null) {
            _state.value = current.copy(formErrorCode = "INVALID_QUANTITY")
            return
        }
        val threshold = current.thresholdInput.trim().toIntOrNull()
        if (threshold == null) {
            _state.value = current.copy(formErrorCode = "INVALID_THRESHOLD")
            return
        }

        loadJob?.cancel()
        _state.value = current.copy(submitting = true, formErrorCode = null)
        viewModelScope.launch {
            val result =
                registerInitialHandler(
                    RegisterInitialInventoryCommand(
                        medicationId = medicationId,
                        initialQuantity = quantity,
                        replenishmentThreshold = threshold,
                    )
                )
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    _state.value =
                        InventoryUiState.Ready(
                            stock = result.value,
                            medicationName = medicationName,
                            unit = unit,
                        )

                is AppResult.Failure ->
                    _state.value =
                        if (InventoryUiState.Error(result.code).blocksEditing)
                            InventoryUiState.Error(result.code)
                        else
                            current.copy(
                                submitting = false,
                                formErrorCode = result.code ?: "REQUEST_FAILED",
                            )
            }
        }
    }

    // --- Replenishment (US-43) ---

    fun onReplenishmentQuantityChange(value: String) = updateReady {
        copy(replenishmentInput = value, replenishmentErrorCode = null, justRegistered = false)
    }

    fun onLotChange(value: String) = updateReady {
        copy(lotInput = value, replenishmentErrorCode = null, justRegistered = false)
    }

    fun saveReplenishment() {
        val current = _state.value as? InventoryUiState.Ready ?: return
        if (current.submitting) return

        val quantity = current.replenishmentInput.trim().toIntOrNull()
        if (quantity == null) {
            _state.value =
                current.copy(replenishmentErrorCode = "INVALID_QUANTITY", justRegistered = false)
            return
        }

        loadJob?.cancel()
        _state.value =
            current.copy(submitting = true, replenishmentErrorCode = null, justRegistered = false)
        viewModelScope.launch {
            val result =
                registerReplenishmentHandler(
                    RegisterReplenishmentCommand(
                        medicationId = medicationId,
                        quantity = quantity,
                        lot = current.lotInput,
                    )
                )
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    _state.value =
                        InventoryUiState.Ready(
                            stock = result.value,
                            medicationName = medicationName,
                            unit = unit,
                            justRegistered = true,
                        )

                is AppResult.Failure ->
                    _state.value =
                        if (InventoryUiState.Error(result.code).blocksEditing)
                            InventoryUiState.Error(result.code)
                        else
                            current.copy(
                                submitting = false,
                                replenishmentErrorCode = result.code ?: "REQUEST_FAILED",
                            )
            }
        }
    }

    private fun load() {
        when (val current = _state.value) {
            is InventoryUiState.Ready -> if (current.submitting) return
            is InventoryUiState.NotInitialized -> if (current.submitting) return
            else -> Unit
        }
        loadJob?.cancel()
        _state.value = InventoryUiState.Loading
        loadJob =
            viewModelScope.launch {
                val result = getStockHandler(GetInventoryStockQuery(medicationId))
                currentCoroutineContext().ensureActive()
                _state.value =
                    when (result) {
                        is AppResult.Success ->
                            InventoryUiState.Ready(
                                stock = result.value,
                                medicationName = medicationName,
                                unit = unit,
                            )

                        is AppResult.Failure ->
                            if (result.code == "INVENTORY_NOT_FOUND") {
                                InventoryUiState.NotInitialized(
                                    medicationName = medicationName,
                                    unit = unit,
                                )
                            } else {
                                InventoryUiState.Error(result.code)
                            }
                    }
            }
    }

    private inline fun updateNotInitialized(
        transform: InventoryUiState.NotInitialized.() -> InventoryUiState.NotInitialized
    ) {
        val current = _state.value as? InventoryUiState.NotInitialized ?: return
        if (current.submitting) return
        _state.value = current.transform()
    }

    private inline fun updateReady(transform: InventoryUiState.Ready.() -> InventoryUiState.Ready) {
        val current = _state.value as? InventoryUiState.Ready ?: return
        if (current.submitting) return
        _state.value = current.transform()
    }

    /** Creates the screen ViewModel with the medication and inventory handlers. */
    class Factory(
        private val medicationId: String,
        private val medicationName: String,
        private val unit: String,
        private val getStockHandler: GetInventoryStockQueryHandler,
        private val registerInitialHandler: RegisterInitialInventoryCommandHandler,
        private val registerReplenishmentHandler: RegisterReplenishmentCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(InventoryViewModel::class.java))
            return InventoryViewModel(
                medicationId = medicationId,
                medicationName = medicationName,
                unit = unit,
                getStockHandler = getStockHandler,
                registerInitialHandler = registerInitialHandler,
                registerReplenishmentHandler = registerReplenishmentHandler,
            )
                as T
        }
    }
}
