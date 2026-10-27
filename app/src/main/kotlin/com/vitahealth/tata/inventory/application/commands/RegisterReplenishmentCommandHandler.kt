package com.vitahealth.tata.inventory.application.commands

import com.vitahealth.tata.inventory.application.ports.InventoryRepository
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.valueobjects.InventoryLot
import com.vitahealth.tata.inventory.domain.model.valueobjects.Quantity
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * US-43. Rejects a non-positive quantity before any network call ("Cantidad inválida"); a valid
 * quantity is delegated to the backend, which updates stock and the low-stock condition.
 */
class RegisterReplenishmentCommandHandler(private val repository: InventoryRepository) {
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

        val quantity =
            Quantity.of(command.quantity)
                ?: return AppResult.Failure(
                    message = "quantity must be greater than zero",
                    code = "INVALID_QUANTITY",
                )

        val rawLot = command.lot?.trim()?.takeIf { it.isNotEmpty() }
        val lot = rawLot?.let(InventoryLot::of)
        if (rawLot != null && lot == null) {
            return AppResult.Failure(
                message = "lot exceeds 200 characters",
                code = "INVALID_LOT",
            )
        }

        return repository.registerReplenishment(
            medicationId = medicationId,
            quantity = quantity.value,
            lot = lot?.value,
        )
    }
}
