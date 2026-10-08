package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.application.commands.UpdateTextSizeCommand
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTextSizeCommandHandlerTest {
    private val remote = FakeUserPreferencesRemote()
    private val handler = UpdateTextSizeCommandHandler(
        OfflineFirstUserPreferencesRepository(remote, FakeAccessibilityLocalStore()),
    )

    @Test
    fun aBlankUserIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = handler(UpdateTextSizeCommand(userId = "  ", textSize = TextSizeLevel.LARGE))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(remote.textSizeCalls.isEmpty())
    }

    @Test
    fun theUserIdIsTrimmedAndTheSizeIsSent() = runBlocking {
        val result = handler(UpdateTextSizeCommand(userId = " user-1 ", textSize = TextSizeLevel.EXTRA_LARGE))

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(TextSizeLevel.EXTRA_LARGE), remote.textSizeCalls)
    }
}
