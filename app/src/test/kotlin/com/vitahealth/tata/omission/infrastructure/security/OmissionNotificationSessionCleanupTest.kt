package com.vitahealth.tata.omission.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.StoredSession
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Checks displayed-notice cleanup independently of Android's notification manager. */
@OptIn(ExperimentalCoroutinesApi::class)
class OmissionNotificationSessionCleanupTest {
    @Test
    fun accountChangesAndLogoutClearNoticesButCredentialRenewalPreservesThem() = runTest {
        val state = MutableStateFlow<StoredSession?>(null)
        var clears = 0
        val cleanup =
            OmissionNotificationSessionCleanup(
                object : SessionObserver {
                    override fun observeSessions() = state
                },
                { clears++ },
            )
        val job = cleanup.start(backgroundScope)
        assertSame(job, cleanup.start(backgroundScope))
        runCurrent()
        assertEquals(1, clears)
        val owner = SessionOwner("a", "CAREGIVER")
        state.value = StoredSession("first", Instant.MAX, owner)
        runCurrent()
        assertEquals(2, clears)
        state.value = StoredSession("renewed", Instant.MAX, owner)
        runCurrent()
        assertEquals(2, clears)
        state.value = StoredSession("other", Instant.MAX, SessionOwner("b", "CAREGIVER"))
        runCurrent()
        assertEquals(3, clears)
        state.value = null
        runCurrent()
        assertEquals(4, clears)
    }

    @Test
    fun expirationClearsWithoutAStorageChangeAndRenewalReplacesTheOldTimer() = runTest {
        val base = Instant.parse("2026-10-23T00:00:00Z")
        val owner = SessionOwner("adult", "OLDER_ADULT")
        val state =
            MutableStateFlow<StoredSession?>(StoredSession("first", base.plusSeconds(10), owner))
        var clears = 0
        val cleanup =
            OmissionNotificationSessionCleanup(
                object : SessionObserver {
                    override fun observeSessions() = state
                },
                { clears++ },
                { base.plusMillis(testScheduler.currentTime) },
            )
        cleanup.start(backgroundScope)
        runCurrent()
        advanceTimeBy(5_000)
        state.value = StoredSession("renewed", base.plusSeconds(20), owner)
        runCurrent()
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(1, clears)
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(2, clears)
    }
}
