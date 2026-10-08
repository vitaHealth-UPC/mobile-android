package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.InventoryRepository
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommand
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.Quantity
import com.vitahealth.tata.inventory.domain.model.ReorderThreshold
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * US-40. Validates the positive-quantity and non-negative-threshold invariants with the domain
 * value objects before delegating. Failure `code`s (not messages) select the UI text.
 */
class RegisterInitialInventoryCommandHandler(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        command: RegisterInitialInventoryCommand,
    ): AppResult<InventoryStockReadModel> {
        val medicationId = command.medicationId.trim()
        if (medicationId.isBlank()) {
            return AppResult.Failure(
                message = "medicationId is required",
                code = "INVALID_MEDICATION_REFERENCE",
            )
        }

        val quantity = Quantity.of(command.initialQuantity)
            ?: return AppResult.Failure(
                message = "initial quantity must be greater than zero",
                code = "INVALID_QUANTITY",
            )

        val threshold = ReorderThreshold.of(command.replenishmentThreshold)
            ?: return AppResult.Failure(
                message = "replenishment threshold cannot be negative",
                code = "INVALID_THRESHOLD",
            )

        return repository.registerInitialInventory(
            medicationId = medicationId,
            initialQuantity = quantity.value,
            replenishmentThreshold = threshold.value,
        )
    }
}
