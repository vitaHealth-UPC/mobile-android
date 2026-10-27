package com.vitahealth.tata.app.integration.consentwithdrawal

import com.vitahealth.tata.shared.application.session.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ConsentWithdrawalIntegrationTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")

    private class Store(var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun save(accessToken: String, expiresAt: String) = Unit

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) = Unit

        override fun clear() {
            value = null
        }
    }

    private fun session() =
        StoredSession("captured", now.plusSeconds(300), SessionOwner("caregiver", "CAREGIVER"))

    @Test
    fun successfulWithdrawalCleansDeviceBeforeClosingCapturedSession() = runBlocking {
        val store =
            Store(StoredSession("old", now.plusSeconds(300), SessionOwner("adult", "OLDER_ADULT")))
        val calls = mutableListOf<String>()
        val action =
            ConsentWithdrawalIntegration(
                store,
                com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal {
                    calls.add("withdraw")
                    AppResult.Success(Unit)
                },
                { calls.add("device") },
                {
                    calls.add("logout")
                    store.clear()
                    AppResult.Failure("Network unavailable")
                },
            )
        assertTrue(action.withdraw("adult") is AppResult.Success)
        assertEquals(listOf("withdraw", "device", "logout"), calls)
        assertNull(store.value)
    }

    @Test
    fun failedWithdrawalDoesNotSignOutAndNewerSessionsSurviveLateWork() = runBlocking {
        for (switchAt in listOf("failure", "withdraw", "device")) {
            val original =
                StoredSession("old", now.plusSeconds(300), SessionOwner("adult", "OLDER_ADULT"))
            val newer = session()
            val store = Store(original)
            var logouts = 0
            var cleanup = 0
            val action =
                ConsentWithdrawalIntegration(
                    store,
                    com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal {
                        if (switchAt == "withdraw") store.value = newer
                        if (switchAt == "failure")
                            AppResult.Failure("Denied", code = "RESOURCE_ACCESS_DENIED")
                        else AppResult.Success(Unit)
                    },
                    {
                        cleanup++
                        if (switchAt == "device") store.value = newer
                    },
                    {
                        logouts++
                        store.clear()
                        AppResult.Success(Unit)
                    },
                )
            assertTrue(action.withdraw("adult") is AppResult.Failure)
            assertEquals(0, logouts)
            assertEquals(if (switchAt == "device") 1 else 0, cleanup)
            assertSame(if (switchAt == "failure") original else newer, store.value)
        }
    }
}
