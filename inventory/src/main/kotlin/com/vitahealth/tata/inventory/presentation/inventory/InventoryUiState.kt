package com.vitahealth.tata.inventory.presentation.inventory

import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel

/**
 * The single Inventory screen across its Figma states. Error text is never stored here: each
 * variant carries a stable error `code`, and the screen resolves it to a localized string.
 */
sealed interface InventoryUiState {
    data object Loading : InventoryUiState

    /**
     * HTTP 404: the medication has no inventory yet. Only the "Definir stock inicial" form is
     * shown (US-40).
     */
    data class NotInitialized(
        val medicationName: String,
        val unit: String,
        val initialQuantityInput: String = "",
        val thresholdInput: String = "",
        val submitting: Boolean = false,
        val formErrorCode: String? = null,
    ) : InventoryUiState

    /** Inventory exists: stock status, replenishment form and last-replenishment summary. */
    data class Ready(
        val stock: InventoryStockReadModel,
        val medicationName: String,
        val unit: String,
        val replenishmentInput: String = "",
        val submitting: Boolean = false,
        val replenishmentErrorCode: String? = null,
        val justRegistered: Boolean = false,
    ) : InventoryUiState

    /** Load failed for a reason other than "not initialized"; the screen offers retry. */
    data class Error(val code: String?) : InventoryUiState
}
