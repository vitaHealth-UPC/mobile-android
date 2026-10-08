package com.vitahealth.tata.preferences.presentation.notifications

import com.vitahealth.tata.preferences.FakeNotificationPreferencesRepository
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.RejectedByServer
import com.vitahealth.tata.preferences.application.handlers.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationPreferencesViewModelTest {
    private val repository = FakeNotificationPreferencesRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = NotificationPreferencesViewModel(
        userId = "user-1",
        getPreferences = GetNotificationPreferencesQueryHandler(repository),
        updatePreferences = UpdateNotificationPreferencesCommandHandler(repository),
    )

    @Test
    fun loadsThePreferencesWhenItOpens() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)

        val model = viewModel()

        assertFalse(model.state.value.isLoading)
        assertEquals(QuietHours.Default, model.state.value.preferences?.quietHours)
    }

    @Test
    fun aFailedLoadShowsTheErrorAndCanBeRetried() {
        repository.failWith = NetworkDown
        val model = viewModel()
        assertNull(model.state.value.preferences)
        assertEquals(NotificationMessage.ErrorOffline, model.state.value.message)

        repository.failWith = null
        model.load()

        assertNotNull(model.state.value.preferences)
        assertNull(model.state.value.message)
    }

    @Test
    fun turningQuietHoursOnStartsFromTheSuggestedInterval() {
        val model = viewModel()

        model.onQuietHoursEnabledChange(true)

        assertEquals(QuietHours.Default, model.state.value.preferences?.quietHours)
        assertEquals(NotificationMessage.Saved, model.state.value.message)
    }

    @Test
    fun turningQuietHoursOffRemovesThem() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)
        val model = viewModel()

        model.onQuietHoursEnabledChange(false)

        assertNull(model.state.value.preferences?.quietHours)
        assertNull(repository.updates.single().quietHours)
    }

    @Test
    fun changingTheStartKeepsTheEnd() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)
        val model = viewModel()

        model.onQuietHoursStartChange(21, 30)

        assertEquals(QuietHours(21, 30, 7, 0), model.state.value.preferences?.quietHours)
    }

    @Test
    fun changingTheEndKeepsTheStart() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)
        val model = viewModel()

        model.onQuietHoursEndChange(8, 15)

        assertEquals(QuietHours(22, 0, 8, 15), model.state.value.preferences?.quietHours)
    }

    @Test
    fun anIntervalThatStartsAndEndsTogetherIsRefusedWithoutCallingTheBackend() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)
        val model = viewModel()

        model.onQuietHoursEndChange(22, 0)

        assertEquals(NotificationMessage.ErrorInvalidHours, model.state.value.message)
        assertTrue(repository.updates.isEmpty())
        assertEquals(QuietHours.Default, model.state.value.preferences?.quietHours)
    }

    @Test
    fun changingTheTimesIsIgnoredWhileQuietHoursAreOff() {
        val model = viewModel()

        model.onQuietHoursStartChange(21, 0)

        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun aChannelCanBeTurnedOnAndTheOthersStay() {
        val model = viewModel()

        model.onChannelChange(ChannelType.EMAIL, true)

        val channels = model.state.value.preferences!!.channels
        assertTrue(channels.first { it.type == ChannelType.EMAIL }.enabled)
        assertTrue(channels.first { it.type == ChannelType.PUSH }.enabled)
        assertEquals(NotificationMessage.Saved, model.state.value.message)
    }

    @Test
    fun aChannelMissingFromTheBackendIsAdded() {
        repository.stored = NotificationPreferences(channels = listOf(NotificationChannel(ChannelType.PUSH, true)))
        val model = viewModel()

        model.onChannelChange(ChannelType.SMS, true)

        assertEquals(2, repository.updates.single().channels.size)
    }

    @Test
    fun repeatingTheCurrentChannelValueDoesNotCallTheBackend() {
        val model = viewModel()

        model.onChannelChange(ChannelType.PUSH, true)

        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun aRejectedSaveKeepsTheOldValueAndShowsTheError() {
        val model = viewModel()
        repository.failWith = RejectedByServer

        model.onChannelChange(ChannelType.SMS, true)

        assertFalse(model.state.value.preferences!!.channels.first { it.type == ChannelType.SMS }.enabled)
        assertEquals(NotificationMessage.ErrorRejected, model.state.value.message)
        assertTrue(model.state.value.messageIsError)
    }

    @Test
    fun savingWithoutConnectionExplainsItIsNeeded() {
        val model = viewModel()
        repository.failWith = NetworkDown

        model.onQuietHoursEnabledChange(true)

        assertNull(model.state.value.preferences?.quietHours)
        assertEquals(NotificationMessage.ErrorOffline, model.state.value.message)
    }
}
