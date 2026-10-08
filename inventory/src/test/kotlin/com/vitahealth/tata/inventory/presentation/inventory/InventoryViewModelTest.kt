package com.vitahealth.tata.inventory.presentation.inventory

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.handlers.GetInventoryStockQueryHandler
import com.vitahealth.tata.inventory.application.handlers.RegisterInitialInventoryCommandHandler
import com.vitahealth.tata.inventory.application.handlers.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = InventoryViewModel(
        medicationId = "med-1",
        medicationName = "Losartán 50 mg",
        unit = "comprimidos",
        getStockHandler = GetInventoryStockQueryHandler(repository),
        registerInitialHandler = RegisterInitialInventoryCommandHandler(repository),
        registerReplenishmentHandler = RegisterReplenishmentCommandHandler(repository),
    )

    @Test
    fun `loads Ready on success`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Success(
            FakeInventoryRepository.sampleReadModel(remainingStock = 35, status = StockStatus.AVAILABLE),
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
    fun `maps not-found to NotInitialized`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Failure(message = "x", code = "INVENTORY_NOT_FOUND")

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is InventoryUiState.NotInitialized)
    }

    @Test
    fun `maps other failure to Error`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Failure(message = "x", code = "REQUEST_FAILED")

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is InventoryUiState.Error)
    }

    @Test
    fun `invalid replenishment quantity does not call the repository`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Success(FakeInventoryRepository.sampleReadModel())

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onReplenishmentQuantityChange("0")
        viewModel.saveReplenishment()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is InventoryUiState.Ready)
        assertEquals("INVALID_QUANTITY", (state as InventoryUiState.Ready).replenishmentErrorCode)
        assertEquals(0, repository.replenishmentCalls)
    }

    @Test
    fun `valid replenishment updates state and flags justRegistered`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Success(FakeInventoryRepository.sampleReadModel())
        repository.replenishmentResult = AppResult.Success(
            FakeInventoryRepository.sampleReadModel(remainingStock = 35, status = StockStatus.AVAILABLE),
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
    fun `invalid threshold in initial form flags INVALID_THRESHOLD without calling the repository`() = runTest(dispatcher) {
        repository.stockResult = AppResult.Failure(message = "x", code = "INVENTORY_NOT_FOUND")

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onInitialQuantityChange("30")
        viewModel.onThresholdChange("-1")
        viewModel.defineInitialInventory()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is InventoryUiState.NotInitialized)
        assertEquals("INVALID_THRESHOLD", (state as InventoryUiState.NotInitialized).formErrorCode)
        assertEquals(0, repository.initialCalls)
    }
}
