package com.vitahealth.tata.inventory.infrastructure.security

import com.vitahealth.tata.inventory.application.ports.InventoryRepository
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Keeps an inventory screen bound to its initiating account and each operation's credentials. */
class SessionScopedInventoryRepository(
    private val sessions: SessionStore,
    private val screenOwner: SessionOwner?,
    private val remoteFor: (StoredSession) -> InventoryRepository,
    private val now: () -> Instant = Instant::now,
) : InventoryRepository {
    override suspend fun getStock(medicationId: String): AppResult<InventoryStockReadModel> =
        scoped {
            it.getStock(medicationId)
        }

    override suspend fun registerInitialInventory(
        medicationId: String,
        initialQuantity: Int,
        replenishmentThreshold: Int,
    ): AppResult<InventoryStockReadModel> = scoped {
        it.registerInitialInventory(medicationId, initialQuantity, replenishmentThreshold)
    }

    override suspend fun registerReplenishment(
        medicationId: String,
        quantity: Int,
        lot: String?,
    ): AppResult<InventoryStockReadModel> = scoped {
        it.registerReplenishment(medicationId, quantity, lot)
    }

    private suspend fun scoped(
        operation: suspend (InventoryRepository) -> AppResult<InventoryStockReadModel>
    ): AppResult<InventoryStockReadModel> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (
            captured == null ||
                owner == null ||
                screenOwner == null ||
                !captured.expiresAt.isAfter(now()) ||
                owner.role == "LINK_SETUP"
        ) {
            return AppResult.Failure(
                "Inicia sesión para consultar el inventario.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner != screenOwner) return sessionChanged()
        val result = operation(remoteFor(captured))
        currentCoroutineContext().ensureActive()
        val current = sessions.session()
        if (
            current?.owner != owner ||
                current.accessToken != captured.accessToken ||
                !current.expiresAt.isAfter(now())
        ) {
            return sessionChanged()
        }
        return result
    }

    private fun sessionChanged() =
        AppResult.Failure(
            "La sesión cambió. Vuelve a abrir el inventario.",
            code = "SESSION_CHANGED",
        )
}
