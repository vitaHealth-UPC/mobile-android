package com.vitahealth.tata.preferences.infrastructure

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.RejectedByServer
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRemote
import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.infrastructure.sync.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstUserPreferencesRepositoryTest {
    private val remote = FakeUserPreferencesRemote()
    private val local = FakeAccessibilityLocalStore()
    private val repository = OfflineFirstUserPreferencesRepository(remote, local)

    @Test
    fun offlineDarkThemeRetriesOnlyItsFieldAndKeepsServerContrast() = runBlocking {
        remote.stored = remote.stored.copy(highContrast = true)
        remote.failWith = NetworkDown
        val update = repository.updateDarkTheme("user-1", true)
        assertFalse((update as AppResult.Success).value.syncedWithServer)
        assertTrue(local.current().darkTheme)
        assertEquals(setOf(AccessibilityField.DARK_THEME), local.pendingFields())
        remote.failWith = null
        assertTrue(repository.sync("user-1") is AppResult.Success)
        assertTrue(local.current().darkTheme)
        assertTrue(local.current().highContrast)
        assertTrue(remote.contrastCalls.isEmpty())
        assertFalse(local.isPendingSync())
    }

    @Test
    fun darkThemeRequiresAnAcknowledgementOfTheRequestedValue() = runBlocking {
        val wrong =
            object : UserPreferencesRemote by remote {
                override suspend fun updateDarkTheme(userId: String, enabled: Boolean) =
                    remote.get(userId)
            }
        val result =
            OfflineFirstUserPreferencesRepository(wrong, local).updateDarkTheme("user-1", true)
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        assertTrue(local.current().darkTheme)
        assertEquals(setOf(AccessibilityField.DARK_THEME), local.pendingFields())
    }

    @Test
    fun rejectedDarkThemeRestoresThePreviousPreference() = runBlocking {
        remote.failWith = RejectedByServer
        assertTrue(repository.updateDarkTheme("user-1", true) is AppResult.Failure)
        assertFalse(local.current().darkTheme)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun unverifiedReceiptDoesNotEraseTheUnacknowledgedIntent() = runBlocking {
        for (code in listOf("INVALID_PREFERENCES_RESPONSE", "REQUEST_FAILED", "SESSION_REQUIRED")) {
            val isolated = FakeAccessibilityLocalStore()
            val failing =
                FakeUserPreferencesRemote().apply {
                    failWith = AppResult.Failure("unverified", code = code)
                }
            val result =
                OfflineFirstUserPreferencesRepository(failing, isolated)
                    .updateHighContrast("user-1", true)
            assertEquals(code, (result as AppResult.Failure).code)
            assertTrue(isolated.current().highContrast)
            assertEquals(setOf(AccessibilityField.HIGH_CONTRAST), isolated.pendingFields())
        }
    }

    @Test
    fun offlineTextSizeDoesNotOverwriteUneditedServerSettings() = runBlocking {
        remote.stored = remote.stored.copy(highContrast = true, reducedMotion = true)
        remote.failWith = NetworkDown
        repository.updateTextSize("user-1", TextSizeLevel.LARGE)
        assertEquals(setOf(AccessibilityField.TEXT_SIZE), local.pendingFields())
        remote.failWith = null
        repository.sync("user-1")
        assertTrue(remote.contrastCalls.isEmpty())
        assertTrue(remote.motionCalls.isEmpty())
        assertTrue(remote.voiceCalls.isEmpty())
        assertTrue(remote.readingCalls.isEmpty())
        assertTrue(local.current().highContrast)
        assertTrue(local.current().reducedMotion)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun retryDoesNotResendFieldsAlreadyAcknowledgedBeforePartialFailure() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateTextSize("user-1", TextSizeLevel.LARGE)
        repository.updateHighContrast("user-1", true)
        remote.failWith = null
        val partial =
            object : UserPreferencesRemote by remote {
                override suspend fun updateHighContrast(
                    userId: String,
                    enabled: Boolean,
                ): AppResult<UserPreferences> = NetworkDown
            }
        val first = OfflineFirstUserPreferencesRepository(partial, local).sync("user-1")
        assertTrue(first is AppResult.Failure)
        assertEquals(setOf(AccessibilityField.HIGH_CONTRAST), local.pendingFields())
        val callsAfterFirst = remote.textSizeCalls.size
        repository.sync("user-1")
        assertEquals(callsAfterFirst, remote.textSizeCalls.size)
        assertFalse(local.isPendingSync())
        assertTrue(local.current().highContrast)
    }

    @Test
    fun wrongFieldReceiptKeepsTheRequestedValuePending() = runBlocking {
        val wrong =
            object : UserPreferencesRemote by remote {
                override suspend fun updateHighContrast(
                    userId: String,
                    enabled: Boolean,
                ): AppResult<UserPreferences> = remote.get(userId)
            }
        val result =
            OfflineFirstUserPreferencesRepository(wrong, local).updateHighContrast("user-1", true)
        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        assertTrue(local.current().highContrast)
        assertEquals(setOf(AccessibilityField.HIGH_CONTRAST), local.pendingFields())
    }

    @Test
    fun receiptForOneFieldDoesNotEraseOlderOfflineChanges() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateTextSize("user-1", TextSizeLevel.LARGE)
        remote.failWith = null
        val result = repository.updateHighContrast("user-1", true) as AppResult.Success
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
        assertTrue(local.current().highContrast)
        assertTrue(local.isPendingSync())
        assertFalse(result.value.syncedWithServer)
        repository.sync("user-1")
        assertEquals(TextSizeLevel.LARGE, remote.stored.textSize)
        assertTrue(remote.stored.highContrast)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun cancellationAfterLocalWritePreservesPendingIntent() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val delayed =
            object : UserPreferencesRemote by remote {
                override suspend fun updateHighContrast(
                    userId: String,
                    enabled: Boolean,
                ): AppResult<UserPreferences> {
                    gate.await()
                    return remote.updateHighContrast(userId, enabled)
                }
            }
        val model = OfflineFirstUserPreferencesRepository(delayed, local)
        val job =
            launch(start = CoroutineStart.UNDISPATCHED) { model.updateHighContrast("user-1", true) }
        assertTrue(local.current().highContrast)
        assertTrue(local.isPendingSync())
        job.cancelAndJoin()
        assertTrue(local.current().highContrast)
        assertTrue(local.isPendingSync())
    }

    @Test
    fun staleSyncReadCannotReplaceAnEditThatArrivedDuringTheRead() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val delayed =
            object : UserPreferencesRemote by remote {
                override suspend fun get(userId: String): AppResult<UserPreferences> {
                    val stale = remote.get(userId)
                    gate.await()
                    return stale
                }
            }
        val model = OfflineFirstUserPreferencesRepository(delayed, local)
        val sync = launch(start = CoroutineStart.UNDISPATCHED) { model.sync("user-1") }
        val edit =
            launch(start = CoroutineStart.UNDISPATCHED) { model.updateHighContrast("user-1", true) }
        assertTrue(remote.contrastCalls.isEmpty())
        gate.complete(Unit)
        sync.join()
        edit.join()
        assertTrue(local.current().highContrast)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun textSizeIsKeptOnTheDeviceAndSentToTheBackend() = runBlocking {
        val result = repository.updateTextSize("user-1", TextSizeLevel.LARGE)

        assertTrue(result is AppResult.Success)
        assertTrue((result as AppResult.Success).value.syncedWithServer)
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
        assertEquals(listOf(TextSizeLevel.LARGE), remote.textSizeCalls)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun withoutConnectionTheChangeStaysOnTheDeviceAndIsMarkedPending() = runBlocking {
        remote.failWith = NetworkDown

        val result = repository.updateTextSize("user-1", TextSizeLevel.LARGE)

        assertTrue(result is AppResult.Success)
        assertFalse((result as AppResult.Success).value.syncedWithServer)
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
        assertTrue(local.isPendingSync())
    }

    @Test
    fun aRejectedChangeIsNotKeptOnTheDevice() = runBlocking {
        remote.failWith = RejectedByServer

        val result = repository.updateTextSize("user-1", TextSizeLevel.LARGE)

        assertTrue(result is AppResult.Failure)
        assertEquals(TextSizeLevel.MEDIUM, local.current().textSize)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun syncSendsAPendingChangeBeforeReadingTheBackend() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateTextSize("user-1", TextSizeLevel.LARGE)
        remote.failWith = null

        val result = repository.sync("user-1")

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(TextSizeLevel.LARGE, TextSizeLevel.LARGE), remote.textSizeCalls)
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun syncReplacesTheDeviceCopyWithTheBackendValue() = runBlocking {
        remote.stored = AccessibilityPreferences(textSize = TextSizeLevel.EXTRA_LARGE)

        repository.sync("user-1")

        assertEquals(TextSizeLevel.EXTRA_LARGE, local.current().textSize)
    }

    @Test
    fun syncKeepsTheDeviceCopyWhenTheBackendCannotBeReached() = runBlocking {
        local.save(AccessibilityPreferences(textSize = TextSizeLevel.LARGE))
        remote.failWith = NetworkDown

        val result = repository.sync("user-1")

        assertTrue(result is AppResult.Failure)
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
    }

    @Test
    fun aPendingChangeIsNotLostWhenTheBackendIsStillDown() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateTextSize("user-1", TextSizeLevel.LARGE)

        repository.sync("user-1")

        assertTrue(local.isPendingSync())
        assertEquals(TextSizeLevel.LARGE, local.current().textSize)
    }

    @Test
    fun highContrastIsKeptOnTheDeviceAndSentToTheBackend() = runBlocking {
        val result = repository.updateHighContrast("user-1", true)

        assertTrue((result as AppResult.Success).value.syncedWithServer)
        assertTrue(local.current().highContrast)
        assertEquals(listOf(true), remote.contrastCalls)
    }

    @Test
    fun highContrastChangedOfflineIsSentOnTheNextSync() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateHighContrast("user-1", true)
        remote.failWith = null

        repository.sync("user-1")

        // the first call failed offline, the second one is the retry made by sync
        assertEquals(listOf(true, true), remote.contrastCalls)
        assertTrue(local.current().highContrast)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun aRejectedHighContrastChangeIsRolledBack() = runBlocking {
        remote.failWith = RejectedByServer

        val result = repository.updateHighContrast("user-1", true)

        assertTrue(result is AppResult.Failure)
        assertFalse(local.current().highContrast)
    }

    @Test
    fun reducedMotionIsKeptOnTheDeviceAndSentToTheBackend() = runBlocking {
        val result = repository.updateReducedMotion("user-1", true)

        assertTrue((result as AppResult.Success).value.syncedWithServer)
        assertTrue(local.current().reducedMotion)
        assertEquals(listOf(true), remote.motionCalls)
    }

    @Test
    fun voiceConfirmationCanBeTurnedOff() = runBlocking {
        val result = repository.updateVoiceConfirmation("user-1", false)

        assertTrue(result is AppResult.Success)
        assertFalse(local.current().voiceConfirmation)
        assertEquals(listOf(false), remote.voiceCalls)
    }

    @Test
    fun everyChangeMadeOfflineIsSentOnTheNextSync() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateReducedMotion("user-1", true)
        repository.updateVoiceConfirmation("user-1", false)
        remote.failWith = null

        repository.sync("user-1")

        assertEquals(true, remote.stored.reducedMotion)
        assertEquals(false, remote.stored.voiceConfirmation)
        assertFalse(local.isPendingSync())
    }

    @Test
    fun aRejectedVoiceChangeIsRolledBack() = runBlocking {
        remote.failWith = RejectedByServer

        val result = repository.updateVoiceConfirmation("user-1", false)

        assertTrue(result is AppResult.Failure)
        assertTrue(local.current().voiceConfirmation)
    }

    @Test
    fun readingAssistanceIsKeptOnTheDeviceAndSentToTheBackend() = runBlocking {
        val result = repository.updateReadingAssistance("user-1", true)

        assertTrue((result as AppResult.Success).value.syncedWithServer)
        assertTrue(local.current().readingAssistance)
        assertEquals(listOf(true), remote.readingCalls)
    }

    @Test
    fun readingAssistanceChangedOfflineReachesTheBackendOnSync() = runBlocking {
        remote.failWith = NetworkDown
        repository.updateReadingAssistance("user-1", true)
        remote.failWith = null

        repository.sync("user-1")

        assertEquals(true, remote.stored.readingAssistance)
        assertFalse(local.isPendingSync())
    }
}
