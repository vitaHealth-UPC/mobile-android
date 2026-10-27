package com.vitahealth.tata.inventory.infrastructure.di

import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQueryHandler
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommandHandler
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.inventory.infrastructure.security.SessionScopedInventoryRepository
import com.vitahealth.tata.inventory.infrastructure.remote.InventoryApiService
import com.vitahealth.tata.inventory.infrastructure.remote.RemoteInventoryRepository
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory

/** Composes inventory operations without sharing a screen's captured owner with another screen. */
internal class InventoryDependencies(
    private val sessions: SessionStore,
    private val baseUrl: String,
) {
    /**
     * Captures the owner when the screen factory is requested and shares its repository across
     * actions.
     */
    fun inventoryViewModelFactory(
        medicationId: String,
        medicationName: String,
        unit: String,
    ): InventoryViewModel.Factory {
        val inventoryRepository =
            SessionScopedInventoryRepository(
                sessions = sessions,
                screenOwner = sessions.session()?.owner,
                remoteFor = { captured ->
                    val api =
                        ApiClientFactory.create(baseUrl) { captured.accessToken }
                            .create(InventoryApiService::class.java)
                    RemoteInventoryRepository(api)
                },
            )
        return InventoryViewModel.Factory(
            medicationId = medicationId,
            medicationName = medicationName,
            unit = unit,
            getStockHandler = GetInventoryStockQueryHandler(inventoryRepository),
            registerInitialHandler = RegisterInitialInventoryCommandHandler(inventoryRepository),
            registerReplenishmentHandler = RegisterReplenishmentCommandHandler(inventoryRepository),
        )
    }
}
