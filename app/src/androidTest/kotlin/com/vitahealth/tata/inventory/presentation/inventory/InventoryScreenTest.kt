package com.vitahealth.tata.inventory.presentation.inventory

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.design.theme.TataTheme
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun replenishmentFocusAndSavingStatesRemainUsable() {
        val date = Instant.parse("2026-09-02T10:00:00Z")
        val stock = InventoryStockReadModel("med-test", 5, 5, StockStatus.LOW,
            listOf(InventoryBatch("batch-test", 30, date, "Lote 2026-09")), date, date, daysRemaining = 5)
        var state by mutableStateOf(InventoryUiState.Ready(stock, "Losartán 50 mg", "comprimidos",
            replenishmentInput = "30", lotInput = "Lote 2026-09"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        compose.setContent {
            TataTheme {
                InventoryScreen(state, onInitialQuantityChange = {}, onThresholdChange = {}, onDefineInitial = {},
                    onReplenishmentChange = { state = state.copy(replenishmentInput = it) },
                    onSaveReplenishment = { state = state.copy(justRegistered = true) }, onRetry = {},
                    onLotChange = { state = state.copy(lotInput = it) }, modifier = Modifier.safeDrawingPadding())
            }
        }
        compose.waitForIdle()
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("screencap -p /data/local/tmp/inventory-native.png")).use { it.readBytes() }
        compose.onNodeWithText(context.getString(R.string.inventory_replenishment_section)).performClick()
        compose.onAllNodes(hasSetTextAction())[0].assertIsFocused()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("45")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("input keyevent 111")).use { it.readBytes() }
        compose.onNodeWithText(context.getString(R.string.inventory_save_replenishment)).performClick()
        compose.onNodeWithText(context.getString(R.string.inventory_replenishment_saved)).assertExists()
        compose.runOnIdle { state = state.copy(submitting = true) }
        compose.onNodeWithText(context.getString(R.string.inventory_saving)).assertIsNotEnabled()
        compose.onNodeWithText("45").assertIsNotEnabled()
        compose.runOnIdle { state = state.copy(submitting = false, justRegistered = false, replenishmentErrorCode = "INVALID_QUANTITY") }
        compose.onNodeWithText(context.getString(R.string.inventory_invalid_quantity_label)).assertExists()
    }
}
