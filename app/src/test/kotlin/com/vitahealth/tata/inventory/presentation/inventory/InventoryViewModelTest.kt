package com.vitahealth.tata.inventory.presentation.inventory

import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.ports.InventoryRepository
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQueryHandler
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommandHandler
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeInventoryRepository()

    @Test
    fun `oversized lot remains editable and is not silently sent as a different lot`() =
        runTest(dispatcher) {
            val model = viewModel()
            advanceUntilIdle()
            val oversized = "L".repeat(201)
            model.onLotChange(oversized)
            model.onReplenishmentQuantityChange("30")
            assertEquals(oversized, (model.state.value as InventoryUiState.Ready).lotInput)
            model.saveReplenishment()
            advanceUntilIdle()
            val invalid = model.state.value as InventoryUiState.Ready
            assertEquals("INVALID_LOT", invalid.replenishmentErrorCode)
            assertEquals(oversized, invalid.lotInput)
            assertEquals(0, repository.replenishmentCalls)
            val valid = "L".repeat(200)
            model.onLotChange(valid)
            assertEquals(null, (model.state.value as InventoryUiState.Ready).replenishmentErrorCode)
            model.saveReplenishment()
            advanceUntilIdle()
            assertEquals(valid, repository.lastReplenishmentLot)
            assertEquals(1, repository.replenishmentCalls)
            model.viewModelScope.cancel()
        }

    @Test
    fun `access failure during either mutation removes stock and prevents another submission`() =
        runTest(dispatcher) {
            for (code in listOf("SESSION_REQUIRED", "SESSION_CHANGED", "ACCESS_DENIED")) {
                for (initial in listOf(true, false)) {
                    repository.stockResult =
                        if (initial) AppResult.Failure("Missing", code = "INVENTORY_NOT_FOUND")
                        else AppResult.Success(FakeInventoryRepository.sampleReadModel())
                    repository.initialResult = AppResult.Failure("Access lost", code = code)
                    repository.replenishmentResult = AppResult.Failure("Access lost", code = code)
                    val model = viewModel()
                    advanceUntilIdle()
                    if (initial) {
                        model.onInitialQuantityChange("30")
                        model.onThresholdChange("5")
                        model.defineInitialInventory()
                    } else {
                        model.onReplenishmentQuantityChange("30")
                        model.saveReplenishment()
                    }
                    advanceUntilIdle()
                    assertEquals(InventoryUiState.Error(code), model.state.value)
                    val calls = repository.initialCalls + repository.replenishmentCalls
                    model.onInitialQuantityChange("60")
                    model.onReplenishmentQuantityChange("60")
                    model.defineInitialInventory()
                    model.saveReplenishment()
                    advanceUntilIdle()
                    assertEquals(calls, repository.initialCalls + repository.replenishmentCalls)
                    assertEquals(InventoryUiState.Error(code), model.state.value)
                    model.viewModelScope.cancel()
                }
            }
        }

    @Test
    fun `non cooperative old load cannot overwrite a newer result`() =
        runTest(dispatcher) {
            lateinit var pending: Continuation<AppResult<InventoryStockReadModel>>
            var reads = 0
            val source =
                object : InventoryRepository by repository {
                    override suspend fun getStock(
                        medicationId: String
                    ): AppResult<InventoryStockReadModel> {
                        reads++
                        return if (reads == 1) suspendCoroutine { pending = it }
                        else
                            AppResult.Success(
                                FakeInventoryRepository.sampleReadModel(remainingStock = 35)
                            )
                    }
                }
            val model = viewModel(source)
            advanceUntilIdle()
            model.retry()
            advanceUntilIdle()
            pending.resume(AppResult.Failure("Old response", code = "INVENTORY_NOT_FOUND"))
            advanceUntilIdle()
            assertEquals(35, (model.state.value as InventoryUiState.Ready).stock.remainingStock)
            model.viewModelScope.cancel()
        }

    @Test
    fun `cancelled mutations cannot publish a late success`() =
        runTest(dispatcher) {
            for (initial in listOf(true, false)) {
                repository.stockResult =
                    if (initial) AppResult.Failure("Missing", code = "INVENTORY_NOT_FOUND")
                    else AppResult.Success(FakeInventoryRepository.sampleReadModel())
                lateinit var pending: Continuation<AppResult<InventoryStockReadModel>>
                val source =
                    object : InventoryRepository by repository {
                        override suspend fun registerInitialInventory(
                            medicationId: String,
                            initialQuantity: Int,
                            replenishmentThreshold: Int,
                        ): AppResult<InventoryStockReadModel> = suspendCoroutine { pending = it }

                        override suspend fun registerReplenishment(
                            medicationId: String,
                            quantity: Int,
                            lot: String?,
                        ): AppResult<InventoryStockReadModel> = suspendCoroutine { pending = it }
                    }
                val model = viewModel(source)
                advanceUntilIdle()
                if (initial) {
                    model.onInitialQuantityChange("30")
                    model.onThresholdChange("5")
                    model.defineInitialInventory()
                } else {
                    model.onReplenishmentQuantityChange("30")
                    model.saveReplenishment()
                }
                advanceUntilIdle()
                val submitting = model.state.value
                model.viewModelScope.cancel()
                pending.resume(
                    AppResult.Success(FakeInventoryRepository.sampleReadModel(remainingStock = 35))
                )
                advanceUntilIdle()
                assertEquals(submitting, model.state.value)
            }
        }

    @Test
    fun `initial stock submission preserves its draft and excludes refresh and duplicate submit`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Failure("Missing", code = "INVENTORY_NOT_FOUND")
            val model = viewModel()
            advanceUntilIdle()
            val pending = CompletableDeferred<AppResult<InventoryStockReadModel>>()
            repository.pendingInitial = pending
            model.onInitialQuantityChange("30")
            model.onThresholdChange("5")
            model.defineInitialInventory()
            advanceUntilIdle()

            model.retry()
            model.onInitialQuantityChange("60")
            model.onThresholdChange("10")
            model.defineInitialInventory()
            advanceUntilIdle()

            assertEquals(1, repository.getStockCalls)
            assertEquals(1, repository.initialCalls)
            pending.complete(AppResult.Failure("No connection", code = "NETWORK_UNAVAILABLE"))
            advanceUntilIdle()

            val state = model.state.value as InventoryUiState.NotInitialized
            assertEquals("30", state.initialQuantityInput)
            assertEquals("5", state.thresholdInput)
            assertEquals("NETWORK_UNAVAILABLE", state.formErrorCode)
            model.onInitialQuantityChange("40")
            assertEquals(
                "40",
                (model.state.value as InventoryUiState.NotInitialized).initialQuantityInput,
            )
        }

    @Test
    fun `superseded stock query cannot replace newer stock`() =
        runTest(dispatcher) {
            val oldLoad = CompletableDeferred<AppResult<InventoryStockReadModel>>()
            repository.pendingStock = oldLoad
            val model = viewModel()
            advanceUntilIdle()
            repository.pendingStock = null
            repository.stockResult =
                AppResult.Success(FakeInventoryRepository.sampleReadModel(remainingStock = 35))

            model.retry()
            advanceUntilIdle()
            oldLoad.complete(
                AppResult.Success(FakeInventoryRepository.sampleReadModel(remainingStock = 5))
            )
            advanceUntilIdle()

            assertEquals(35, (model.state.value as InventoryUiState.Ready).stock.remainingStock)
        }

    @Test
    fun `replenishment excludes refresh edits and duplicate submit until it finishes`() =
        runTest(dispatcher) {
            val model = viewModel()
            advanceUntilIdle()
            val pending = CompletableDeferred<AppResult<InventoryStockReadModel>>()
            repository.pendingReplenishment = pending
            model.onReplenishmentQuantityChange("30")
            model.onLotChange("LOT-1")
            model.saveReplenishment()
            advanceUntilIdle()

            model.retry()
            model.onReplenishmentQuantityChange("60")
            model.onLotChange("LOT-2")
            model.saveReplenishment()
            advanceUntilIdle()

            assertEquals(1, repository.getStockCalls)
            assertEquals(1, repository.replenishmentCalls)
            assertEquals("30", (model.state.value as InventoryUiState.Ready).replenishmentInput)
            pending.complete(AppResult.Failure("No connection", code = "NETWORK_UNAVAILABLE"))
            advanceUntilIdle()
            val state = model.state.value as InventoryUiState.Ready
            assertEquals("30", state.replenishmentInput)
            assertEquals("LOT-1", state.lotInput)
            assertEquals("NETWORK_UNAVAILABLE", state.replenishmentErrorCode)
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repository: InventoryRepository = this.repository) =
        InventoryViewModel(
            medicationId = "med-1",
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
            getStockHandler = GetInventoryStockQueryHandler(repository),
            registerInitialHandler = RegisterInitialInventoryCommandHandler(repository),
            registerReplenishmentHandler = RegisterReplenishmentCommandHandler(repository),
        )

    @Test
    fun `loads Ready on success`() =
        runTest(dispatcher) {
            repository.stockResult =
                AppResult.Success(
                    FakeInventoryRepository.sampleReadModel(
                        remainingStock = 35,
                        status = StockStatus.AVAILABLE,
                    )
                )

            val viewModel = viewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state is InventoryUiState.Ready)
            state as InventoryUiState.Ready
            assertEquals(35, state.stock.remainingStock)
            assertEquals("Losartán 50 mg", state.medicationName)
        }

    @Test
    fun `maps not-found to NotInitialized`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Failure(message = "x", code = "INVENTORY_NOT_FOUND")

            val viewModel = viewModel()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is InventoryUiState.NotInitialized)
        }

    @Test
    fun `maps other failure to Error`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Failure(message = "x", code = "REQUEST_FAILED")

            val viewModel = viewModel()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is InventoryUiState.Error)
        }

    @Test
    fun `invalid replenishment quantity does not call the repository`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Success(FakeInventoryRepository.sampleReadModel())

            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onReplenishmentQuantityChange("0")
            viewModel.saveReplenishment()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state is InventoryUiState.Ready)
            assertEquals(
                "INVALID_QUANTITY",
                (state as InventoryUiState.Ready).replenishmentErrorCode,
            )
            assertEquals(0, repository.replenishmentCalls)
        }

    @Test
    fun `valid replenishment updates state and flags justRegistered`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Success(FakeInventoryRepository.sampleReadModel())
            repository.replenishmentResult =
                AppResult.Success(
                    FakeInventoryRepository.sampleReadModel(
                        remainingStock = 35,
                        status = StockStatus.AVAILABLE,
                    )
                )

            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onReplenishmentQuantityChange("30")
            viewModel.saveReplenishment()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state is InventoryUiState.Ready)
            state as InventoryUiState.Ready
            assertTrue(state.justRegistered)
            assertEquals(35, state.stock.remainingStock)
            assertEquals(1, repository.replenishmentCalls)
        }

    @Test
    fun `invalid threshold in initial form flags INVALID_THRESHOLD without calling the repository`() =
        runTest(dispatcher) {
            repository.stockResult = AppResult.Failure(message = "x", code = "INVENTORY_NOT_FOUND")

            val viewModel = viewModel()
            advanceUntilIdle()
            viewModel.onInitialQuantityChange("30")
            viewModel.onThresholdChange("-1")
            viewModel.defineInitialInventory()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertTrue(state is InventoryUiState.NotInitialized)
            assertEquals(
                "INVALID_THRESHOLD",
                (state as InventoryUiState.NotInitialized).formErrorCode,
            )
            assertEquals(0, repository.initialCalls)
        }
}
