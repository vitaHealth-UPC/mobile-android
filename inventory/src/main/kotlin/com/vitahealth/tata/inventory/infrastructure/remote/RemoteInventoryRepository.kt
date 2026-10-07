package com.vitahealth.tata.inventory.infrastructure.remote

import com.vitahealth.tata.inventory.application.InventoryRepository
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import com.google.gson.Gson
import retrofit2.Response
import java.time.Instant

/**
 * Talks to the web-services Inventory API and maps responses into read models.
 *
 * Errors use the stable backend error code, with a fallback per endpoint and HTTP status. Each stable
 * [AppResult.Failure.code] distinguishes inactive medications from duplicate inventory. `message`s are English
 * diagnostics; the presentation layer selects the user-facing text from the `code`.
 */
class RemoteInventoryRepository(
    private val api: InventoryApiService,
) : InventoryRepository {

    override suspend fun getStock(medicationId: String): AppResult<InventoryStockReadModel> =
        request(
            call = { api.getStock(medicationId) },
            errorFor = { status ->
                when (status) {
                    404 -> "INVENTORY_NOT_FOUND" to "inventory not found"
                    else -> "REQUEST_FAILED" to "could not load inventory (HTTP $status)"
                }
            },
        )

    override suspend fun registerInitialInventory(
        medicationId: String,
        initialQuantity: Int,
        replenishmentThreshold: Int,
    ): AppResult<InventoryStockReadModel> =
        request(
            call = {
                api.registerInitialInventory(
                    RegisterInitialInventoryRequest(
                        medicationId = medicationId,
                        initialQuantity = initialQuantity,
                        replenishmentThreshold = replenishmentThreshold,
                    ),
                )
            },
            errorFor = { status ->
                when (status) {
                    400 -> "INVALID_QUANTITY" to "invalid quantity or threshold"
                    404 -> "MEDICATION_NOT_FOUND" to "medication not found"
                    409 -> "INVENTORY_ALREADY_EXISTS" to "inventory already exists for this medication"
                    else -> "REQUEST_FAILED" to "could not register initial inventory (HTTP $status)"
                }
            },
        )

    override suspend fun registerReplenishment(
        medicationId: String,
        quantity: Int,
        lot: String?,
    ): AppResult<InventoryStockReadModel> =
        request(
            call = {
                api.registerReplenishment(
                    medicationId = medicationId,
                    request = RegisterReplenishmentRequest(quantity = quantity, lot = lot),
                )
            },
            errorFor = { status ->
                when (status) {
                    400 -> "INVALID_QUANTITY" to "invalid quantity"
                    404 -> "INVENTORY_NOT_FOUND" to "inventory not found"
                    409 -> "CONCURRENT_UPDATE" to "inventory was modified concurrently; retry"
                    else -> "REQUEST_FAILED" to "could not register replenishment (HTTP $status)"
                }
            },
        )

    private suspend fun request(
        call: suspend () -> Response<InventoryResponse>,
        errorFor: (Int) -> Pair<String, String>,
    ): AppResult<InventoryStockReadModel> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                toReadModel(body)
            } else {
                val (code, message) = errorFor(response.code())
                val backendCode = runCatching {
                    response.errorBody()?.charStream()?.use {
                        Gson().fromJson(it, ErrorEnvelope::class.java)?.code
                    }
                }.getOrNull()?.takeIf { it in knownErrorCodes }
                AppResult.Failure(message = message, code = backendCode ?: code)
            }
        } catch (exception: java.util.concurrent.CancellationException) {
            throw exception
        } catch (exception: java.io.IOException) {
            AppResult.Failure(
                message = "no network connection",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(message = "invalid inventory response", cause = exception, code = "INVALID_RESPONSE")
        }
    }

    private fun toReadModel(body: InventoryResponse): AppResult<InventoryStockReadModel> {
        val createdAt = parseInstant(body.createdAt)
        val updatedAt = parseInstant(body.updatedAt)
        if (createdAt == null || updatedAt == null) {
            return AppResult.Failure(
                message = "unparseable inventory timestamps",
                code = "INVALID_RESPONSE",
            )
        }

        val batches = body.batches.map { dto ->
            val registeredAt = parseInstant(dto.registeredAt)
                ?: return AppResult.Failure(message = "unparseable batch timestamp", code = "INVALID_RESPONSE")
            InventoryBatch(id = dto.id, quantity = dto.quantity, registeredAt = registeredAt, lot = dto.lot)
        }

        return AppResult.Success(
            InventoryStockReadModel(
                medicationId = body.medicationId,
                remainingStock = body.remainingStock,
                replenishmentThreshold = body.replenishmentThreshold,
                status = StockStatus.fromLowStockFlag(body.lowStock),
                batches = batches,
                createdAt = createdAt,
                updatedAt = updatedAt,
                daysRemaining = body.daysRemaining,
            ),
        )
    }

    private data class ErrorEnvelope(val code: String?)
    private val knownErrorCodes = setOf(
        "MEDICATION_NOT_FOUND", "MEDICATION_INACTIVE", "INVENTORY_NOT_FOUND",
        "INVENTORY_ALREADY_EXISTS", "INVALID_QUANTITY", "INSUFFICIENT_STOCK",
        "CONCURRENT_UPDATE", "VALIDATION_ERROR",
    )

    private fun parseInstant(raw: String): Instant? =
        runCatching { Instant.parse(raw) }.getOrNull()
}
