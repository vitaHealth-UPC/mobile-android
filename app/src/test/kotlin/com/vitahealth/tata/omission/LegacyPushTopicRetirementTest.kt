package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.ports.LegacyPushTopicSource
import com.vitahealth.tata.omission.application.ports.PushTopicRemover
import com.vitahealth.tata.omission.application.services.LegacyPushTopicRetirement
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LegacyPushTopicRetirementTest {
    @Test
    fun waitsForAcknowledgmentAndRetriesOnlyFailedTopics() = runTest {
        val calls = mutableListOf<String>()
        var firstFailure = true
        val service =
            LegacyPushTopicRetirement(
                LegacyPushTopicSource { linkedSetOf("user-old", "caregivers-of-other") },
                PushTopicRemover { topic ->
                    calls += topic
                    if (topic == "user-old" && firstFailure) {
                        firstFailure = false
                        AppResult.Failure("offline", code = "PUSH_RETIREMENT_FAILED")
                    } else AppResult.Success(Unit)
                },
                retryMillis = 100,
            )
        val job = service.start(backgroundScope)
        runCurrent()
        assertEquals(listOf("user-old", "caregivers-of-other"), calls)
        assertFalse(job.isCompleted)
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf("user-old", "caregivers-of-other", "user-old"), calls)
        assertTrue(job.isCompleted)
    }

    @Test
    fun neverReportsAnUnacknowledgedRequestAsFinishedAndStartsOnlyOneCollector() = runTest {
        val acknowledgment = CompletableDeferred<AppResult<Unit>>()
        var calls = 0
        val service =
            LegacyPushTopicRetirement(
                LegacyPushTopicSource { setOf("user-old") },
                PushTopicRemover {
                    calls++
                    acknowledgment.await()
                },
            )
        val job = service.start(backgroundScope)
        runCurrent()
        assertSame(job, service.start(backgroundScope))
        assertFalse(job.isCompleted)
        assertEquals(1, calls)
        acknowledgment.complete(AppResult.Success(Unit))
        runCurrent()
        assertTrue(job.isCompleted)
    }

    @Test
    fun missingFirebaseStopsWithoutClaimingRemovalAndRetriesOnNextProcessStart() = runTest {
        var calls = 0
        val service =
            LegacyPushTopicRetirement(
                LegacyPushTopicSource { setOf("user-old") },
                PushTopicRemover {
                    calls++
                    AppResult.Failure("not configured", code = "PUSH_NOT_CONFIGURED")
                },
            )
        service.start(backgroundScope)
        runCurrent()
        assertEquals(1, calls)
        service.start(backgroundScope)
        runCurrent()
        assertEquals(2, calls)
    }
}
