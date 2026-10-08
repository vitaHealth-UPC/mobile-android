package com.vitahealth.tata.preferences.presentation.accessibility

import com.vitahealth.tata.preferences.FakeAccessibilityLocalStore
import com.vitahealth.tata.preferences.FakeUserPreferencesRemote
import com.vitahealth.tata.preferences.NetworkDown
import com.vitahealth.tata.preferences.RejectedByServer
import com.vitahealth.tata.preferences.application.handlers.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateHighContrastCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateReadingAssistanceCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateReducedMotionCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateVoiceConfirmationCommandHandler
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.preferences.infrastructure.OfflineFirstUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccessibilityViewModelTest {
    private val remote = FakeUserPreferencesRemote()
    private val local = FakeAccessibilityLocalStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(userId: String = "user-1"): AccessibilityViewModel {
        val repository = OfflineFirstUserPreferencesRepository(remote, local)
        return AccessibilityViewModel(
            userId = userId,
            observeAccessibility = ObserveAccessibilityPreferencesQueryHandler(repository),
            updateTextSize = UpdateTextSizeCommandHandler(repository),
            updateHighContrast = UpdateHighContrastCommandHandler(repository),
            updateReducedMotion = UpdateReducedMotionCommandHandler(repository),
            updateVoiceConfirmation = UpdateVoiceConfirmationCommandHandler(repository),
            updateReadingAssistance = UpdateReadingAssistanceCommandHandler(repository),
            syncPreferences = SyncUserPreferencesCommandHandler(repository),
        )
    }

    @Test
    fun startsFromTheDeviceCopyAndThenTheBackendValue() {
        remote.stored = AccessibilityPreferences(textSize = TextSizeLevel.LARGE)

        val model = viewModel()

        assertTrue(model.state.value.preferences.largeTextEnabled)
        assertEquals(1, remote.getCalls)
    }

    @Test
    fun turningLargeTextOnSavesItAndShowsTheConfirmation() {
        val model = viewModel()

        model.onLargeTextChange(true)

        assertTrue(model.state.value.preferences.largeTextEnabled)
        assertEquals(AccessibilityMessage.LargeTextSaved, model.state.value.message)
        assertFalse(model.state.value.isSaving)
    }

    @Test
    fun turningLargeTextOffGoesBackToTheStandardSize() {
        local.setInitialLarge()
        val model = viewModel()

        model.onLargeTextChange(false)

        assertEquals(TextSizeLevel.MEDIUM, model.state.value.preferences.textSize)
        assertEquals(AccessibilityMessage.StandardTextSaved, model.state.value.message)
    }

    @Test
    fun withoutConnectionTheChangeStillAppliesAndSaysItWasSavedOnTheDevice() {
        val model = viewModel()
        remote.failWith = NetworkDown

        model.onLargeTextChange(true)

        assertTrue(model.state.value.preferences.largeTextEnabled)
        assertEquals(AccessibilityMessage.SavedOffline, model.state.value.message)
        assertFalse(model.state.value.messageIsError)
    }

    @Test
    fun aRejectedChangeShowsAnErrorAndKeepsTheOldValue() {
        val model = viewModel()
        remote.failWith = RejectedByServer

        model.onLargeTextChange(true)

        assertFalse(model.state.value.preferences.largeTextEnabled)
        assertEquals(AccessibilityMessage.ErrorRejected, model.state.value.message)
        assertTrue(model.state.value.messageIsError)
    }

    @Test
    fun repeatingTheCurrentValueDoesNotCallTheBackendAgain() {
        val model = viewModel()
        model.onLargeTextChange(true)

        model.onLargeTextChange(true)

        assertEquals(1, remote.textSizeCalls.size)
    }

    @Test
    fun aNewChangeReplacesThePreviousMessage() {
        val model = viewModel()
        model.onLargeTextChange(true)
        remote.failWith = NetworkDown

        model.onLargeTextChange(false)

        assertEquals(AccessibilityMessage.SavedOffline, model.state.value.message)
    }

    @Test
    fun turningHighContrastOnSavesItAndShowsTheConfirmation() {
        val model = viewModel()

        model.onHighContrastChange(true)

        assertTrue(model.state.value.preferences.highContrast)
        assertEquals(AccessibilityMessage.HighContrastSaved, model.state.value.message)
    }

    @Test
    fun turningHighContrastOffRestoresTheStandardContrast() {
        val model = viewModel()
        model.onHighContrastChange(true)

        model.onHighContrastChange(false)

        assertFalse(model.state.value.preferences.highContrast)
        assertEquals(AccessibilityMessage.StandardContrastSaved, model.state.value.message)
    }

    @Test
    fun highContrastChangedWithoutConnectionStillApplies() {
        val model = viewModel()
        remote.failWith = NetworkDown

        model.onHighContrastChange(true)

        assertTrue(model.state.value.preferences.highContrast)
        assertEquals(AccessibilityMessage.SavedOffline, model.state.value.message)
    }

    @Test
    fun aRejectedHighContrastChangeKeepsTheOldValue() {
        val model = viewModel()
        remote.failWith = RejectedByServer

        model.onHighContrastChange(true)

        assertFalse(model.state.value.preferences.highContrast)
        assertEquals(AccessibilityMessage.ErrorRejected, model.state.value.message)
    }

    @Test
    fun turningReducedMotionOnSavesItAndShowsTheConfirmation() {
        val model = viewModel()

        model.onReducedMotionChange(true)

        assertTrue(model.state.value.preferences.reducedMotion)
        assertEquals(AccessibilityMessage.ReducedMotionSaved, model.state.value.message)
    }

    @Test
    fun turningReducedMotionOffRestoresTheAnimations() {
        val model = viewModel()
        model.onReducedMotionChange(true)

        model.onReducedMotionChange(false)

        assertFalse(model.state.value.preferences.reducedMotion)
        assertEquals(AccessibilityMessage.StandardMotionSaved, model.state.value.message)
    }

    @Test
    fun voiceConfirmationStartsOnAndCanBeTurnedOff() {
        val model = viewModel()
        assertTrue(model.state.value.preferences.voiceConfirmation)

        model.onVoiceConfirmationChange(false)

        assertFalse(model.state.value.preferences.voiceConfirmation)
        assertEquals(AccessibilityMessage.VoiceConfirmationOffSaved, model.state.value.message)
    }

    @Test
    fun voiceConfirmationChangedWithoutConnectionStillApplies() {
        val model = viewModel()
        remote.failWith = NetworkDown

        model.onVoiceConfirmationChange(false)

        assertFalse(model.state.value.preferences.voiceConfirmation)
        assertEquals(AccessibilityMessage.SavedOffline, model.state.value.message)
    }

    @Test
    fun aRejectedMotionChangeKeepsTheOldValue() {
        val model = viewModel()
        remote.failWith = RejectedByServer

        model.onReducedMotionChange(true)

        assertFalse(model.state.value.preferences.reducedMotion)
        assertEquals(AccessibilityMessage.ErrorRejected, model.state.value.message)
    }

    @Test
    fun turningReadingAssistanceOnSavesItAndShowsTheConfirmation() {
        val model = viewModel()

        model.onReadingAssistanceChange(true)

        assertTrue(model.state.value.preferences.readingAssistance)
        assertEquals(AccessibilityMessage.ReadingAssistanceSaved, model.state.value.message)
    }

    @Test
    fun turningReadingAssistanceOffRestoresTheStandardText() {
        val model = viewModel()
        model.onReadingAssistanceChange(true)

        model.onReadingAssistanceChange(false)

        assertFalse(model.state.value.preferences.readingAssistance)
        assertEquals(AccessibilityMessage.StandardReadingSaved, model.state.value.message)
    }

    @Test
    fun aRejectedReadingChangeKeepsTheOldValue() {
        val model = viewModel()
        remote.failWith = RejectedByServer

        model.onReadingAssistanceChange(true)

        assertFalse(model.state.value.preferences.readingAssistance)
        assertEquals(AccessibilityMessage.ErrorRejected, model.state.value.message)
    }

    private fun FakeAccessibilityLocalStore.setInitialLarge() {
        kotlinx.coroutines.runBlocking { save(AccessibilityPreferences(textSize = TextSizeLevel.LARGE)) }
        remote.stored = AccessibilityPreferences(textSize = TextSizeLevel.LARGE)
    }
}
