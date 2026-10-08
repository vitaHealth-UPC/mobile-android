package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.application.commands.UpdateVoiceConfirmationCommand
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVoiceConfirmationCommandHandlerTest {
    private val remote = FakeUserPreferencesRemote()
    private val handler = UpdateVoiceConfirmationCommandHandler(
        OfflineFirstUserPreferencesRepository(remote, FakeAccessibilityLocalStore()),
    )

    @Test
    fun aBlankUserIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(UpdateVoiceConfirmationCommand(userId = " ", enabled = true))

        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(remote.voiceCalls.isEmpty())
    }

    @Test
    fun theChosenValueIsSent() = runBlocking {
        val result = handler(UpdateVoiceConfirmationCommand(userId = " user-1 ", enabled = false))

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(false), remote.voiceCalls)
    }
}
