package com.vitahealth.tata.carelink.presentation.profiles.viewmodel

import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.GenerateCareLinkCodeCommandHandler
import com.vitahealth.tata.carelink.application.commands.RegisterOlderAdultCommandHandler
import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQueryHandler
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
class CaregiverProfilesViewModelTest {
    @Test
    fun returningRechecksLinksWithoutDuplicatingInitialLoadOrOverwritingTheForm() {
        var calls = 0
        val repository =
            java.lang.reflect.Proxy.newProxyInstance(
                CaregiverProfilesRepository::class.java.classLoader,
                arrayOf(CaregiverProfilesRepository::class.java),
            ) { _, method, _ ->
                check(method.name == "list")
                calls++
                AppResult.Success(
                    if (calls == 1) listOf(LinkedAdult("adult", "Rosa"))
                    else emptyList<LinkedAdult>()
                )
            } as CaregiverProfilesRepository
        val model = model(repository)
        model.onResume()
        assertEquals(1, calls)
        model.onResume()
        assertEquals(2, calls)
        assertTrue(model.state.value.adults.isEmpty())
        model.form()
        model.name("Ana")
        model.onResume()
        assertEquals(2, calls)
        assertEquals("Ana", model.state.value.name)
        assertTrue(model.state.value.showForm)
        model.viewModelScope.cancel()
    }

    @Test
    fun cancelledRegistrationDoesNotPublishIdentityOrRequestLinkingCode() {
        lateinit var pending: Continuation<AppResult<LinkedAdult>>
        var codeCalls = 0
        val repository =
            object : CaregiverProfilesRepository {
                override suspend fun list(caregiverId: String) =
                    AppResult.Success(emptyList<LinkedAdult>())

                override suspend fun register(
                    caregiverId: String,
                    profile: NewAdultProfile,
                ): AppResult<LinkedAdult> = suspendCoroutine { pending = it }

                override suspend fun linkingCode(
                    caregiverId: String,
                    adult: LinkedAdult,
                ): AppResult<ProfileLinkingCode> {
                    codeCalls++
                    return AppResult.Failure("Unexpected", code = "REQUEST_FAILED")
                }
            }
        val model = draft(repository)
        model.save()
        val beforeCancellation = model.state.value
        model.viewModelScope.cancel()
        pending.resume(AppResult.Success(LinkedAdult("adult", "Rosa")))
        assertEquals(beforeCancellation, model.state.value)
        assertEquals(0, codeCalls)
    }

    @Test
    fun losingAccountAccessClearsPreviouslyLoadedProfilesAndDraft() {
        for (code in
            listOf("AUTHENTICATION_REQUIRED", "RESOURCE_ACCESS_DENIED", "SESSION_CHANGED")) {
            var calls = 0
            val repository =
                java.lang.reflect.Proxy.newProxyInstance(
                    CaregiverProfilesRepository::class.java.classLoader,
                    arrayOf(CaregiverProfilesRepository::class.java),
                ) { _, _, _ ->
                    calls++
                    if (calls == 1) AppResult.Success(listOf(LinkedAdult("adult", "Rosa")))
                    else AppResult.Failure("Access ended", code = code)
                } as CaregiverProfilesRepository
            val model = model(repository)
            model.form()
            model.name("Draft")
            model.refresh()
            assertTrue(model.state.value.requiresAccountRecovery)
            assertTrue(model.state.value.adults.isEmpty())
            assertEquals("", model.state.value.name)
            assertFalse(model.state.value.showForm)
            assertFalse(model.state.value.busy)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun failedCodeGenerationRetriesTheCreatedProfileWithoutRegisteringAgain() {
        val repository = RecordingRepository()
        val model = draft(repository)

        model.save()
        model.name("Another person")
        model.date(LocalDate.of(1970, 1, 1))
        model.phone("999999999")
        model.save()

        assertEquals(1, repository.registrations)
        assertEquals(listOf(repository.adult, repository.adult), repository.codeAdults)
        assertEquals("Rosa", model.state.value.name)
        assertEquals(LocalDate.of(1958, 5, 12), model.state.value.birthDate)
        assertEquals("", model.state.value.phone)
        assertEquals("ABC123", model.state.value.code?.code)
        assertFalse(model.state.value.busy)
        assertFalse(model.state.value.showForm)
    }

    @Test
    fun registrationInFlightRejectsDuplicateSubmissionAndDraftEdits() {
        val pending = CompletableDeferred<AppResult<LinkedAdult>>()
        val repository = RecordingRepository(pending)
        val model = draft(repository)

        model.save()
        model.name("Another person")
        model.save()
        assertTrue(model.state.value.busy)
        assertEquals(1, repository.registrations)
        assertEquals("Rosa", model.state.value.name)

        pending.complete(AppResult.Success(repository.adult))

        assertFalse(model.state.value.busy)
        assertEquals(repository.adult, model.state.value.createdAdult)
    }

    private fun model(repository: CaregiverProfilesRepository) =
        CaregiverProfilesViewModel(
            caregiverId = "caregiver",
            listProfiles = ListCaregiverProfilesQueryHandler(repository),
            registerAdult = RegisterOlderAdultCommandHandler(repository),
            generateCode = GenerateCareLinkCodeCommandHandler(repository),
        )

    private fun draft(repository: CaregiverProfilesRepository) =
        model(repository).apply {
            form()
            name("Rosa")
            date(LocalDate.of(1958, 5, 12))
        }

    private class RecordingRepository(
        private val pending: CompletableDeferred<AppResult<LinkedAdult>>? = null
    ) : CaregiverProfilesRepository {
        val adult = LinkedAdult("adult", "Rosa")
        var registrations = 0
        val codeAdults = mutableListOf<LinkedAdult>()

        override suspend fun list(caregiverId: String) = AppResult.Success(emptyList<LinkedAdult>())

        override suspend fun register(
            caregiverId: String,
            profile: NewAdultProfile,
        ): AppResult<LinkedAdult> {
            registrations++
            return pending?.await() ?: AppResult.Success(adult)
        }

        override suspend fun linkingCode(
            caregiverId: String,
            adult: LinkedAdult,
        ): AppResult<ProfileLinkingCode> {
            codeAdults += adult
            return if (codeAdults.size == 1) {
                AppResult.Failure("No connection", code = "NETWORK_UNAVAILABLE")
            } else {
                AppResult.Success(
                    ProfileLinkingCode(adult.id, adult.name, "ABC123", "2099-01-01T00:00:00Z")
                )
            }
        }
    }
}
