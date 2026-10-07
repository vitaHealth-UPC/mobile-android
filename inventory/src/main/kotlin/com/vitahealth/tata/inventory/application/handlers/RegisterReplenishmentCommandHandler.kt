package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.InventoryRepository
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommand
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.Quantity
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * US-43. Rejects a non-positive quantity before any network call ("Cantidad inválida"); a valid
 * quantity is delegated to the backend, which updates stock and the low-stock condition.
 */
class RegisterReplenishmentCommandHandler(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        command: RegisterReplenishmentCommand,
    ): AppResult<InventoryStockReadModel> {
        val medicationId = command.medicationId.trim()
        if (medicationId.isBlank()) {
            return AppResult.Failure(
                message = "medicationId is required",
                code = "INVALID_MEDICATION_REFERENCE",
            )
        }

        val quantity = Quantity.of(command.quantity)
            ?: return AppResult.Failure(
                message = "quantity must be greater than zero",
                code = "INVALID_QUANTITY",
            )

        if ((command.lot?.length ?: 0) > 200) return AppResult.Failure(message = "lot exceeds 200 characters", code = "VALIDATION_ERROR")
        return repository.registerReplenishment(
            medicationId = medicationId,
            quantity = quantity.value,
            lot = command.lot?.trim()?.takeIf { it.isNotBlank() },
        )
    }
}
