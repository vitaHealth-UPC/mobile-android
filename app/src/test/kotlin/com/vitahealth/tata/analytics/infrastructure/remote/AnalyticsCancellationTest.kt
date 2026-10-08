package com.vitahealth.tata.analytics.infrastructure.remote

import java.lang.reflect.Proxy
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class AnalyticsCancellationTest {
    private val cancellation = CancellationException("screen closed")
    private val api = Proxy.newProxyInstance(
        AnalyticsApiService::class.java.classLoader,
        arrayOf(AnalyticsApiService::class.java),
    ) { _, _, _ -> throw cancellation } as AnalyticsApiService

    @Test
    fun summaryPreservesCancellationWhenLeavingScreen() {
        val thrown = assertThrows(CancellationException::class.java) {
            runBlocking { RemoteAdherenceSummaryRepository(api).getSummary("adult", 7) }
        }
        assertSame(cancellation, thrown)
    }

    @Test
    fun recommendationsPreserveCancellationWhenLeavingScreen() {
        val thrown = assertThrows(CancellationException::class.java) {
            runBlocking { RemoteAdherenceRecommendationsRepository(api).getRecommendations("adult", 7) }
        }
        assertSame(cancellation, thrown)
    }
}
