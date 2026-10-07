package com.vitahealth.tata.inventory.infrastructure.remote

import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant

class RemoteInventoryRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteInventoryRepository

    @Test
    fun `invalid batch dates reject the response instead of silently dropping batches`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""
            {"id":"inv-1","medicationId":"med-1","remainingStock":5,
             "replenishmentThreshold":2,"lowStock":false,
             "batches":[{"id":"b1","quantity":5,"registeredAt":"invalid"}],
             "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}
        """.trimIndent()))
        val result = repository.getStock("med-1")
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    @Test
    fun `screen cancellation is propagated`() = runTest {
        val cancellation = java.util.concurrent.CancellationException("screen closed")
        val api = java.lang.reflect.Proxy.newProxyInstance(
            InventoryApiService::class.java.classLoader, arrayOf(InventoryApiService::class.java),
        ) { _, _, _ -> throw cancellation } as InventoryApiService
        var caught: java.util.concurrent.CancellationException? = null
        try {
            RemoteInventoryRepository(api).getStock("med-1")
        } catch (exception: java.util.concurrent.CancellationException) {
            caught = exception
        }
        org.junit.Assert.assertSame(cancellation, caught)
    }

    @Test
    fun `inactive medication is not reported as duplicate stock`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409)
            .setBody("""{"code":"MEDICATION_INACTIVE","message":"inactive"}"""))
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("MEDICATION_INACTIVE", (result as AppResult.Failure).code)
    }

    @Test
    fun `unknown medication keeps its backend error code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404)
            .setBody("""{"code":"MEDICATION_NOT_FOUND","message":"missing"}"""))
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("MEDICATION_NOT_FOUND", (result as AppResult.Failure).code)
    }

    @Test
    fun `malformed error body preserves the status fallback`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("unavailable"))
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("INVENTORY_ALREADY_EXISTS", (result as AppResult.Failure).code)
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InventoryApiService::class.java)
        repository = RemoteInventoryRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getStock maps a 200 response into a read model`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "id": "inv-1",
                  "medicationId": "med-1",
                  "remainingStock": 5,
                  "replenishmentThreshold": 5,
                  "lowStock": true,
                  "batches": [
                    { "id": "b1", "quantity": 30, "registeredAt": "2026-09-02T10:00:00Z" }
                  ],
                  "createdAt": "2026-08-01T10:00:00Z",
                  "updatedAt": "2026-09-02T10:00:00Z"
                }
                """.trimIndent(),
            ),
        )

        val result = repository.getStock("med-1")

        assertTrue(result is AppResult.Success)
        val model = (result as AppResult.Success).value
        assertEquals(5, model.remainingStock)
        assertEquals(StockStatus.LOW, model.status)
        assertEquals(1, model.batches.size)
        assertEquals(Instant.parse("2026-09-02T10:00:00Z"), model.lastReplenishment?.registeredAt)
        assertNull(model.daysRemaining)
    }

    @Test
    fun `getStock maps 404 to INVENTORY_NOT_FOUND`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"code":"INVENTORY_NOT_FOUND","message":"x"}"""))

        val result = repository.getStock("med-1")

        assertTrue(result is AppResult.Failure)
        assertEquals("INVENTORY_NOT_FOUND", (result as AppResult.Failure).code)
    }

    @Test
    fun `replenishment maps 400 to INVALID_QUANTITY`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"code":"INVALID_QUANTITY","message":"x"}"""))

        val result = repository.registerReplenishment("med-1", 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_QUANTITY", (result as AppResult.Failure).code)
    }

    @Test
    fun `replenishment maps 409 to CONCURRENT_UPDATE`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"code":"CONCURRENT_UPDATE","message":"x"}"""))

        val result = repository.registerReplenishment("med-1", 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("CONCURRENT_UPDATE", (result as AppResult.Failure).code)
    }

    @Test
    fun `initial inventory maps 409 to INVENTORY_ALREADY_EXISTS`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"code":"INVENTORY_ALREADY_EXISTS","message":"x"}"""))

        val result = repository.registerInitialInventory("med-1", 30, 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("INVENTORY_ALREADY_EXISTS", (result as AppResult.Failure).code)
    }
}
