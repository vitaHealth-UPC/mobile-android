package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests the production client converter and the contact use case together. */
class ContactHttpBoundaryTest {
    @Test
    fun malformedProfileCannotBecomeAValidContactFallback() = runBlocking {
        for (profile in listOf("{\"id\":", """{"id":"adult","fullName":{}}""")) {
            MockWebServer().use { server ->
                server.enqueue(
                    MockResponse().setBody("""{"type":"PHONE","value":"+51999888777"}""")
                )
                server.enqueue(MockResponse().setBody(profile))
                val api =
                    ApiClientFactory.create(server.url("/").toString()) { "token" }
                        .create(FamilyMonitoringApiService::class.java)
                val result =
                    GetContactOptionQueryHandler(RemoteContactRepository(api))(
                        GetContactOptionQuery("caregiver", "adult")
                    )
                assertEquals(AlertFailureCodes.INVALID_RESPONSE, (result as AppResult.Failure).code)
                assertEquals(2, server.requestCount)
            }
        }
    }
}
