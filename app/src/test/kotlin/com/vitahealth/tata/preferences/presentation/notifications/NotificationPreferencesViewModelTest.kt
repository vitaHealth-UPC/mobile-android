package com.vitahealth.tata.preferences.presentation.notifications

import com.vitahealth.tata.preferences.FakeNotificationPreferencesRepository
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.RejectedByServer
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.domain.repositories.NotificationPreferencesRepository
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
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

    private fun viewModel(repo: NotificationPreferencesRepository = repository) =
        NotificationPreferencesViewModel(
            userId = "user-1",
            getPreferences = GetNotificationPreferencesQueryHandler(repo),
            updatePreferences = UpdateNotificationPreferencesCommandHandler(repo),
            updatePolicy =
                com.vitahealth.tata.preferences.application.commands.UpdateNotificationPolicyCommandHandler(repo),
        )

    @Test
    fun canceledInitialLoadCannotOverwriteAConfirmedEdit() {
        var initial: Continuation<AppResult<NotificationPreferences>>? = null
        var calls = 0
        val delayed =
            object : NotificationPreferencesRepository by repository {
                override suspend fun get(userId: String): AppResult<NotificationPreferences> {
                    calls++
                    return if (calls == 1) suspendCoroutine { initial = it }
                    else repository.get(userId)
                }
            }
        val model = viewModel(delayed)
        model.load()
        model.onChannelChange(ChannelType.SMS, true)
        initial!!.resume(AppResult.Success(NotificationPreferences()))
        assertTrue(
            model.state.value.preferences!!.channels.first { it.type == ChannelType.SMS }.enabled
        )
        assertEquals(NotificationMessage.Saved, model.state.value.message)
    }

    @Test
    fun reloadAndOtherEditsAreIgnoredDuringAnActiveSave() {
        val gate = CompletableDeferred<Unit>()
        val delayed =
            object : NotificationPreferencesRepository by repository {
                override suspend fun update(
                    userId: String,
                    preferences: NotificationPreferences,
                ): AppResult<NotificationPreferences> {
                    gate.await()
                    return repository.update(userId, preferences)
                }
            }
        val model = viewModel(delayed)
        model.onChannelChange(ChannelType.SMS, true)
        assertTrue(model.state.value.isSaving)
        model.load()
        model.onQuietHoursEnabledChange(true)
        assertEquals(1, repository.getCalls)
        assertNull(model.state.value.message)
        gate.complete(Unit)
        assertFalse(model.state.value.isSaving)
        assertTrue(
            model.state.value.preferences!!.channels.first { it.type == ChannelType.SMS }.enabled
        )
        assertNull(model.state.value.preferences!!.quietHours)
    }

    @Test
    fun loadsThePreferencesWhenItOpens() {
        repository.stored = NotificationPreferences(quietHours = QuietHours.Default)

        val model = viewModel()

        assertFalse(model.state.value.isLoading)
        assertEquals(QuietHours.Default, model.state.value.preferences?.quietHours)
    }

    @Test
    fun policySavePreservesChannelsAndQuietHours() {
        val policy =
            com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy(
                10,
                20,
                120,
                180,
                20,
                0,
            )
        repository.stored =
            NotificationPreferences(quietHours = QuietHours.Default, policy = policy)
        val model = viewModel()
        model.onPolicyChange(policy.copy(reminderMinutes = 15), emptyList())
        assertEquals(15, model.state.value.preferences!!.policy!!.reminderMinutes)
        assertEquals(QuietHours.Default, model.state.value.preferences!!.quietHours)
        assertTrue(model.state.value.preferences!!.channels.first().enabled)
        assertEquals(NotificationMessage.Saved, model.state.value.message)
        model.onChannelChange(ChannelType.SMS, true)
        assertEquals(15, model.state.value.preferences!!.policy!!.reminderMinutes)
    }

    @Test
    fun rejectedPolicySaveKeepsTheServerValues() {
        val policy =
            com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy(
                10,
                20,
                120,
                180,
                20,
                0,
            )
        repository.stored = NotificationPreferences(policy = policy)
        val model = viewModel()
        repository.failWith = RejectedByServer
        model.onPolicyChange(policy.copy(reminderMinutes = 15), emptyList())
        assertEquals(policy, model.state.value.preferences!!.policy)
        assertEquals(NotificationMessage.ErrorRejected, model.state.value.message)
        assertFalse(model.state.value.isSaving)
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
        repository.stored =
            NotificationPreferences(channels = listOf(NotificationChannel(ChannelType.PUSH, true)))
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

        assertFalse(
            model.state.value.preferences!!.channels.first { it.type == ChannelType.SMS }.enabled
        )
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
