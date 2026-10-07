package com.vitahealth.tata.preferences.infrastructure

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.RejectedByServer
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.shared.common.result.AppResult
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
}
