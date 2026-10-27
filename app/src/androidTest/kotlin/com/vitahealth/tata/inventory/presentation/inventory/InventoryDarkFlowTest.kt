package com.vitahealth.tata.inventory.presentation.inventory

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import com.vitahealth.tata.inventory.presentation.inventory.screens.InventoryScreen
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Inventory state, actions and visual theme checks using production composables.
 *
 * Invented values here are isolated to androidTest. Domain and server commands
 * are not replaced; remote authorization requires separate integration tests.
 */
@RunWith(AndroidJUnit4::class)
class InventoryDarkFlowTest {
    @get:Rule val compose = createComposeRule()

    private val date = Instant.parse("2026-09-02T10:00:00Z")
    private val stock =
        InventoryStockReadModel(
            "med-test",
            5,
            5,
            StockStatus.LOW,
            listOf(InventoryBatch("batch-test", 30, date, "Lote 2026-09")),
            date,
            date,
            daysRemaining = 5,
        )

    private fun ready(submitting: Boolean = false) =
        InventoryUiState.Ready(
            stock = stock,
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
            replenishmentInput = "30",
            lotInput = "Lote 2026-09",
            submitting = submitting,
        )

    @Test
    fun readyStockDark() {
        show(ready(), dark = true)
        compose.onNodeWithText("Losartán 50 mg").assertExists()
        capture("ready-dark")
    }

    @Test
    fun readyStockLight() {
        show(ready(), dark = false)
        compose.onNodeWithText("Losartán 50 mg").assertExists()
        capture("ready-light")
    }

    @Test
    fun lastReplenishmentIsReadableAfterNativeScrolling() {
        // One batch is initial stock; only a second recorded batch is a replenishment.
        val replenishedStock =
            stock.copy(
                batches =
                    listOf(
                        InventoryBatch("initial-batch", 20, date.minusSeconds(86400), "Inicial"),
                        InventoryBatch("new-batch", 30, date, "Lote 2026-09"),
                    ),
            )
        show(ready().copy(stock = replenishedStock), dark = true)
        val spanish =
            InstrumentationRegistry.getInstrumentation().targetContext.withSpanishAppLocale()
        compose.onNodeWithText(spanish.getString(R.string.inventory_last_replenishment))
            .performScrollTo()
            .assertIsDisplayed()
        capture("ready-batch-dark")
    }

    @Test
    fun initialStockDark() {
        show(
            InventoryUiState.NotInitialized(
                medicationName = "Losartán 50 mg",
                unit = "comprimidos",
                initialQuantityInput = "20",
                thresholdInput = "4",
            ),
            dark = true,
        )
        capture("initial-dark")
    }

    @Test
    fun accessDeniedHidesStockAndReplenishmentForm() {
        show(InventoryUiState.Error("ACCESS_DENIED"), dark = true)
        compose.onNodeWithText("Losartán 50 mg").assertDoesNotExist()
        val spanish =
            InstrumentationRegistry.getInstrumentation().targetContext.withSpanishAppLocale()
        compose.onNodeWithText(spanish.getString(R.string.inventory_quantity_received_label))
            .assertDoesNotExist()
        capture("access-denied-dark")
    }

    @Test
    fun saveReplenishmentInvokesHandler() {
        var invoked = false
        show(ready(), dark = true, onSave = { invoked = true })
        val spanish =
            InstrumentationRegistry.getInstrumentation().targetContext.withSpanishAppLocale()
        compose.onNodeWithText(spanish.getString(R.string.inventory_save_replenishment))
            .performScrollTo()
            .performClick()
        compose.runOnIdle { assertTrue("Save callback not invoked", invoked) }
    }

    @Test
    fun savingPreventsDuplicateRequest() {
        show(ready(submitting = true), dark = true)
        val spanish =
            InstrumentationRegistry.getInstrumentation().targetContext.withSpanishAppLocale()
        compose.onNodeWithText(spanish.getString(R.string.inventory_saving))
            .assertIsNotEnabled()
    }

    private fun show(
        state: InventoryUiState,
        dark: Boolean,
        onSave: () -> Unit = {},
    ) {
        compose.setContent {
            val base = LocalContext.current
            val spanish = remember(base) { base.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme(TataAccessibility(darkTheme = dark)) {
                    InventoryScreen(
                        state = state,
                        olderAdultName = "Rosa Vargas",
                        onInitialQuantityChange = {},
                        onThresholdChange = {},
                        onDefineInitial = {},
                        onReplenishmentChange = {},
                        onSaveReplenishment = onSave,
                        onRetry = {},
                    )
                }
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap =
            try {
                compose.onRoot().captureToImage().asAndroidBitmap()
            } catch (failure: AssertionError) {
                requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
                    "Cannot capture Inventory state: " + name
                }
            }
        check(bitmap.width > 100 && bitmap.height > 100)
        val edge = bitmap.getPixel(4, bitmap.height / 2)
        val brightness =
            (Color.red(edge) + Color.green(edge) + Color.blue(edge)) / 3
        assertTrue(
            "Inventory background has wrong theme brightness: " + brightness,
            if (name.endsWith("-dark")) brightness < 110 else brightness > 190,
        )
        val resolver = instrumentation.targetContext.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "tata-inventory-" + name + ".png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
                },
            )
        )
        resolver.openOutputStream(uri)!!.use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
