package com.vitahealth.tata.inventory.infrastructure.remote

import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
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

class RemoteInventoryRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: RemoteInventoryRepository

    @Test
    fun `authorization status takes precedence over stock error envelopes on every endpoint`() =
        runTest {
            for ((status, expected) in listOf(401 to "SESSION_REQUIRED", 403 to "ACCESS_DENIED")) {
                repeat(3) {
                    server.enqueue(
                        MockResponse()
                            .setResponseCode(status)
                            .setBody("""{"code":"INVENTORY_NOT_FOUND"}""")
                    )
                }
                val results =
                    listOf(
                        repository.getStock("med-1"),
                        repository.registerInitialInventory("med-1", 30, 5),
                        repository.registerReplenishment("med-1", 30, "lot"),
                    )
                results.forEach { assertEquals(expected, (it as AppResult.Failure).code) }
            }
        }

    @Test
    fun `missing and null stock fields are rejected instead of becoming zero or false`() = runTest {
        val valid =
            """{"id":"inv-1","medicationId":"med-1","remainingStock":0,
            "replenishmentThreshold":0,"lowStock":false,"batches":[],
            "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}"""
        for (field in listOf("remainingStock", "replenishmentThreshold", "lowStock")) {
            for (explicitNull in listOf(false, true)) {
                val payload = com.google.gson.JsonParser.parseString(valid).asJsonObject
                if (explicitNull) payload.add(field, com.google.gson.JsonNull.INSTANCE)
                else payload.remove(field)
                server.enqueue(MockResponse().setResponseCode(200).setBody(payload.toString()))
                assertEquals(
                    field,
                    "INVALID_RESPONSE",
                    (repository.getStock("med-1") as AppResult.Failure).code,
                )
            }
        }
        server.enqueue(MockResponse().setResponseCode(200).setBody(valid))
        val stock = (repository.getStock("med-1") as AppResult.Success).value
        assertEquals(0, stock.remainingStock)
        assertEquals(0, stock.replenishmentThreshold)
        assertEquals(StockStatus.AVAILABLE, stock.status)
    }

    @Test
    fun `repeated batch identities reject the whole response`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
            {"id":"inv-1","medicationId":"med-1","remainingStock":5,
            "replenishmentThreshold":2,"lowStock":false,"batches":[
            {"id":"batch","quantity":3,"registeredAt":"2026-08-01T10:00:00Z"},
            {"id":"batch","quantity":2,"registeredAt":"2026-08-01T11:00:00Z"}],
            "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}
        """
                        .trimIndent()
                )
        )
        assertEquals("INVALID_RESPONSE", (repository.getStock("med-1") as AppResult.Failure).code)
    }

    @Test
    fun `malformed JSON is not reported as a network outage`() = runTest {
        val api =
            java.lang.reflect.Proxy.newProxyInstance(
                InventoryApiService::class.java.classLoader,
                arrayOf(InventoryApiService::class.java),
            ) { _, _, _ ->
                throw com.google.gson.stream.MalformedJsonException("Invalid JSON")
            } as InventoryApiService
        val result = RemoteInventoryRepository(api).getStock("med-1")
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    @Test
    fun `all endpoints reject stock belonging to another medication`() = runTest {
        val body =
            """
            {"id":"inv-1","medicationId":"another-medication","remainingStock":5,
             "replenishmentThreshold":2,"lowStock":false,"batches":[],
             "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}
        """
                .trimIndent()
        repeat(3) { server.enqueue(MockResponse().setResponseCode(200).setBody(body)) }

        val results =
            listOf(
                repository.getStock("med-1"),
                repository.registerInitialInventory("med-1", 5, 2),
                repository.registerReplenishment("med-1", 5, null),
            )

        results.forEach { assertEquals("INVALID_RESPONSE", (it as AppResult.Failure).code) }
    }

    @Test
    fun `invalid quantities and identities are not exposed as inventory`() = runTest {
        val valid =
            InventoryResponse(
                "inv-1",
                "med-1",
                5,
                2,
                false,
                listOf(BatchResponse("batch-1", 5, "2026-08-01T10:00:00Z")),
                "2026-08-01T10:00:00Z",
                "2026-09-02T10:00:00Z",
                5,
                1,
            )
        for (body in
            listOf(
                valid.copy(id = ""),
                valid.copy(remainingStock = -1),
                valid.copy(replenishmentThreshold = -1),
                valid.copy(daysRemaining = -1),
                valid.copy(dailyConsumptionUnits = -1),
                valid.copy(batches = listOf(valid.batches.single().copy(quantity = 0))),
                valid.copy(batches = listOf(valid.batches.single().copy(id = ""))),
            )) {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(com.google.gson.Gson().toJson(body))
            )

            val result = repository.getStock("med-1")

            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
    }

    @Test
    fun `invalid batch dates reject the response instead of silently dropping batches`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
            {"id":"inv-1","medicationId":"med-1","remainingStock":5,
             "replenishmentThreshold":2,"lowStock":false,
             "batches":[{"id":"b1","quantity":5,"registeredAt":"invalid"}],
             "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}
        """
                        .trimIndent()
                )
        )
        val result = repository.getStock("med-1")
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    @Test
    fun `screen cancellation is propagated`() = runTest {
        val cancellation = java.util.concurrent.CancellationException("screen closed")
        val api =
            java.lang.reflect.Proxy.newProxyInstance(
                InventoryApiService::class.java.classLoader,
                arrayOf(InventoryApiService::class.java),
            ) { _, _, _ ->
                throw cancellation
            } as InventoryApiService
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
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody("""{"code":"MEDICATION_INACTIVE","message":"inactive"}""")
        )
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("MEDICATION_INACTIVE", (result as AppResult.Failure).code)
    }

    @Test
    fun `unknown medication keeps its backend error code`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"code":"MEDICATION_NOT_FOUND","message":"missing"}""")
        )
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("MEDICATION_NOT_FOUND", (result as AppResult.Failure).code)
    }

    @Test
    fun `malformed error body preserves the status fallback`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("unavailable"))
        val result = repository.registerInitialInventory("med-1", 30, 5)
        assertEquals("INVENTORY_ALREADY_EXISTS", (result as AppResult.Failure).code)
    }

    @Test
    fun `replenishment sends lot and reads backend coverage`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(
                    """
            {"id":"inv-1","medicationId":"med-1","remainingStock":30,
             "replenishmentThreshold":5,"lowStock":false,"daysRemaining":15,"dailyConsumptionUnits":2,
             "batches":[
                {"id":"initial","quantity":10,"registeredAt":"2026-08-01T10:00:00Z"},
                {"id":"b1","quantity":30,"lot":"2026-09","registeredAt":"2026-09-02T10:00:00Z"}
              ],
             "createdAt":"2026-08-01T10:00:00Z","updatedAt":"2026-09-02T10:00:00Z"}
        """
                        .trimIndent()
                )
        )
        val result = repository.registerReplenishment("med-1", 30, "2026-09") as AppResult.Success
        assertEquals(15, result.value.daysRemaining)
        assertEquals("2026-09", result.value.lastReplenishment?.lot)
        val request = server.takeRequest()
        val payload = com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals("2026-09", payload.get("lot").asString)
        assertEquals(30, payload.get("quantity").asInt)
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api =
            Retrofit.Builder()
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
            MockResponse()
                .setResponseCode(200)
                .setBody(
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
                """
                        .trimIndent()
                )
        )

        val result = repository.getStock("med-1")

        assertTrue(result is AppResult.Success)
        val model = (result as AppResult.Success).value
        assertEquals(5, model.remainingStock)
        assertEquals(StockStatus.LOW, model.status)
        assertEquals(1, model.batches.size)
        assertNull(model.lastReplenishment) // One initial batch is not a replenishment.
        assertNull(model.daysRemaining)
    }

    @Test
    fun `getStock maps 404 to INVENTORY_NOT_FOUND`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"code":"INVENTORY_NOT_FOUND","message":"x"}""")
        )

        val result = repository.getStock("med-1")

        assertTrue(result is AppResult.Failure)
        assertEquals("INVENTORY_NOT_FOUND", (result as AppResult.Failure).code)
    }

    @Test
    fun `replenishment maps 400 to INVALID_QUANTITY`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"code":"INVALID_QUANTITY","message":"x"}""")
        )

        val result = repository.registerReplenishment("med-1", 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_QUANTITY", (result as AppResult.Failure).code)
    }

    @Test
    fun `replenishment maps 409 to CONCURRENT_UPDATE`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody("""{"code":"CONCURRENT_UPDATE","message":"x"}""")
        )

        val result = repository.registerReplenishment("med-1", 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("CONCURRENT_UPDATE", (result as AppResult.Failure).code)
    }

    @Test
    fun `initial inventory maps 409 to INVENTORY_ALREADY_EXISTS`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody("""{"code":"INVENTORY_ALREADY_EXISTS","message":"x"}""")
        )

        val result = repository.registerInitialInventory("med-1", 30, 5)

        assertTrue(result is AppResult.Failure)
        assertEquals("INVENTORY_ALREADY_EXISTS", (result as AppResult.Failure).code)
    }
}
