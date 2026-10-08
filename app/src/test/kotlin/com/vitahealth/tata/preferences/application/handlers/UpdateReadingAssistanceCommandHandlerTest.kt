package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.application.commands.UpdateReadingAssistanceCommand
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateReadingAssistanceCommandHandlerTest {
    private val remote = FakeUserPreferencesRemote()
    private val handler = UpdateReadingAssistanceCommandHandler(
        OfflineFirstUserPreferencesRepository(remote, FakeAccessibilityLocalStore()),
    )

    @Test
    fun aBlankUserIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(UpdateReadingAssistanceCommand(userId = "", enabled = true))

        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(remote.readingCalls.isEmpty())
    }

    @Test
    fun theChosenValueIsSent() = runBlocking {
        val result = handler(UpdateReadingAssistanceCommand(userId = " user-1 ", enabled = true))

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(true), remote.readingCalls)
    }
}
