package com.vitahealth.tata.inventory.presentation.inventory.screens

import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import java.time.Instant

// --- Previews of the Figma states ---

private fun sampleStock(remaining: Int, status: StockStatus) =
    InventoryStockReadModel(
        medicationId = "med-1",
        remainingStock = remaining,
        replenishmentThreshold = 5,
        status = status,
        batches =
            listOf(
                InventoryBatch(
                    id = "b1",
                    quantity = 30,
                    registeredAt = Instant.parse("2026-09-02T10:00:00Z"),
                )
            ),
        createdAt = Instant.parse("2026-08-01T10:00:00Z"),
        updatedAt = Instant.parse("2026-09-02T10:00:00Z"),
        daysRemaining = null,
    )

private val noop: () -> Unit = {}
private val noopStr: (String) -> Unit = {}

@Preview(name = "Low stock", showBackground = true)
@Composable
private fun InventoryLowStockPreview() {
    InventoryScreen(
        state =
            InventoryUiState.Ready(
                stock = sampleStock(5, StockStatus.LOW),
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Stock available", showBackground = true)
@Composable
private fun InventoryAvailablePreview() {
    InventoryScreen(
        state =
            InventoryUiState.Ready(
                stock = sampleStock(35, StockStatus.AVAILABLE),
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
                replenishmentInput = "30",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Invalid quantity", showBackground = true)
@Composable
private fun InventoryInvalidQuantityPreview() {
    InventoryScreen(
        state =
            InventoryUiState.Ready(
                stock = sampleStock(5, StockStatus.LOW),
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
                replenishmentErrorCode = "INVALID_QUANTITY",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Not initialized (404)", showBackground = true)
@Composable
private fun InventoryNotInitializedPreview() {
    InventoryScreen(
        state =
            InventoryUiState.NotInitialized(
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Stock bajo (es-419)", locale = "b+es+419", showBackground = true)
@Composable
private fun InventoryLowStockEsPreview() {
    InventoryScreen(
        state =
            InventoryUiState.Ready(
                stock = sampleStock(5, StockStatus.LOW),
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "No inicializado (es-419)", locale = "b+es+419", showBackground = true)
@Composable
private fun InventoryNotInitializedEsPreview() {
    InventoryScreen(
        state =
            InventoryUiState.NotInitialized(
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
            ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}
