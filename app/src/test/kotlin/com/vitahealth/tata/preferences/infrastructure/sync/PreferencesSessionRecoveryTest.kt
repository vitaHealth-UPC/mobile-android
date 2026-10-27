package com.vitahealth.tata.preferences.infrastructure.sync

import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.StoredSession
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesSessionRecoveryTest {
    private val now = Instant.parse("2026-10-24T12:00:00Z")

    private fun session(
        id: String,
        role: String = "CAREGIVER",
        expires: Instant = now.plusSeconds(300),
    ) = StoredSession("token-$id", expires, SessionOwner(id, role))

    @Test
    fun loginAndReconnectSynchronizeWithoutAnOpenScreen() = runTest {
        val sessions = MutableStateFlow<StoredSession?>(null)
        val online = MutableStateFlow(false)
        val calls = mutableListOf<String>()
        val observer =
            object : SessionObserver {
                override fun observeSessions() = sessions
            }
        val recovery = PreferencesSessionRecovery(observer, online, { calls += it }, { now })
        val job = recovery.start(backgroundScope)
        assertSame(job, recovery.start(backgroundScope))
        sessions.value = session("first")
        runCurrent()
        assertTrue(calls.isEmpty())
        online.value = true
        runCurrent()
        assertEquals(listOf("first"), calls)
        online.value = false
        runCurrent()
        online.value = true
        runCurrent()
        assertEquals(listOf("first", "first"), calls)
        sessions.value = session("second", "OLDER_ADULT")
        runCurrent()
        assertEquals(listOf("first", "first", "second"), calls)
    }

    @Test
    fun logoutCancelsRecoveryAndUnusableSessionsDoNotSynchronize() = runTest {
        val sessions = MutableStateFlow<StoredSession?>(session("first"))
        val calls = mutableListOf<String>()
        var cancelled = false
        val observer =
            object : SessionObserver {
                override fun observeSessions() = sessions
            }
        PreferencesSessionRecovery(
                observer,
                MutableStateFlow(true),
                {
                    calls += it
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled = true
                    }
                },
                { now },
            )
            .start(backgroundScope)
        runCurrent()
        sessions.value = null
        runCurrent()
        assertTrue(cancelled)
        sessions.value = session("setup", "LINK_SETUP")
        runCurrent()
        sessions.value = session("expired", expires = now)
        runCurrent()
        assertEquals(listOf("first"), calls)
    }
}
