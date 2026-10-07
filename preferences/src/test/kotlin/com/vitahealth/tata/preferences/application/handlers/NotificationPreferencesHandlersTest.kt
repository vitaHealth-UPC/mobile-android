package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.FakeNotificationPreferencesRepository
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommand
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQuery
import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPreferencesHandlersTest {
    private val repository = FakeNotificationPreferencesRepository()
    private val get = GetNotificationPreferencesQueryHandler(repository)
    private val update = UpdateNotificationPreferencesCommandHandler(repository)

    private val channels = listOf(
        NotificationChannel(ChannelType.PUSH, true),
        NotificationChannel(ChannelType.SMS, false),
    )

    @Test
    fun readingWithABlankUserIsRejected() = runBlocking {
        val result = get(GetNotificationPreferencesQuery(" "))

        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertEquals(0, repository.getCalls)
    }

    @Test
    fun readingReturnsWhatTheBackendHolds() = runBlocking {
        val result = get(GetNotificationPreferencesQuery(" user-1 "))

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.getCalls)
    }

    @Test
    fun updatingWithABlankUserIsRejected() = runBlocking {
        val result = update(UpdateNotificationPreferencesCommand("", null, channels))

        assertEquals("INVALID_USER_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun aRepeatedChannelIsRejectedBeforeCallingTheBackend() = runBlocking {
        val repeated = channels + NotificationChannel(ChannelType.PUSH, false)

        val result = update(UpdateNotificationPreferencesCommand("user-1", null, repeated))

        assertEquals("DUPLICATE_CHANNEL", (result as AppResult.Failure).code)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun quietHoursAndChannelsAreSentTogether() = runBlocking {
        val result = update(UpdateNotificationPreferencesCommand("user-1", QuietHours.Default, channels))

        assertTrue(result is AppResult.Success)
        assertEquals(QuietHours.Default, repository.updates.single().quietHours)
        assertEquals(channels, repository.updates.single().channels)
    }

    @Test
    fun removingTheQuietHoursSendsNull() = runBlocking {
        update(UpdateNotificationPreferencesCommand("user-1", null, channels))

        assertNull(repository.updates.single().quietHours)
    }
}
