package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.application.commands.UpdateReducedMotionCommand
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateReducedMotionCommandHandlerTest {
    private val remote = FakeUserPreferencesRemote()
    private val handler = UpdateReducedMotionCommandHandler(
        OfflineFirstUserPreferencesRepository(remote, FakeAccessibilityLocalStore()),
    )

    @Test
    fun aBlankUserIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(UpdateReducedMotionCommand(userId = " ", enabled = true))

        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(remote.motionCalls.isEmpty())
    }

    @Test
    fun theChosenValueIsSent() = runBlocking {
        val result = handler(UpdateReducedMotionCommand(userId = " user-1 ", enabled = false))

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(false), remote.motionCalls)
    }
}
