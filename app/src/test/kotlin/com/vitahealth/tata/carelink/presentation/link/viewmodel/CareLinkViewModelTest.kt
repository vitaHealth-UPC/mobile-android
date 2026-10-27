package com.vitahealth.tata.carelink.presentation.link.viewmodel

import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareLinkViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun delayedPreviewCannotReplaceTheAcceptedAdult() {
        val preview = CompletableDeferred<AppResult<OlderAdultProfile>>()
        val acceptedAdult = profile("accepted-adult")
        val repository =
            object : CareLinkRepository {
                override suspend fun acceptLink(caregiverId: String, code: String) =
                    AppResult.Success(
                        CareLink(
                            "link",
                            caregiverId,
                            acceptedAdult.id,
                            CareLinkStatus.PENDING,
                            null,
                            null,
                            false,
                            null,
                            null,
                        )
                    )

                override suspend fun registerConsent(
                    careLinkId: String,
                    accepted: Boolean,
                ): AppResult<CareLink> = error("Unused")

                override suspend fun getOlderAdult(
                    olderAdultId: String
                ): AppResult<OlderAdultProfile> =
                    if (olderAdultId == acceptedAdult.id) AppResult.Success(acceptedAdult)
                    else preview.await()
            }
        val model =
            CareLinkViewModel(
                "caregiver",
                AcceptCareLinkCommandHandler(repository),
                RegisterConsentCommandHandler(repository),
                GetOlderAdultProfileQueryHandler(repository),
            )

        model.previewOlderAdult("preview-adult")
        model.onCodeChange("TATA-4821")
        model.sendLinkRequest()
        preview.complete(AppResult.Success(profile("preview-adult")))

        assertEquals(CareLinkStep.AwaitingConsent, model.state.value.step)
        assertEquals(acceptedAdult, model.state.value.olderAdult)
    }

    private fun profile(id: String) =
        OlderAdultProfile(id, "Rosa Vargas", LocalDate.of(1958, 1, 1), null, null, null)

    @Test
    fun submittedCodeUsesTheReturnedLinkStatus() {
        val expectedSteps =
            mapOf(
                CareLinkStatus.PENDING to CareLinkStep.AwaitingConsent,
                CareLinkStatus.AWAITING_CONSENT to CareLinkStep.AwaitingConsent,
                CareLinkStatus.CONFIRMED to CareLinkStep.Confirmed,
                CareLinkStatus.REVOKED to CareLinkStep.Rejected,
            )
        for ((status, expected) in expectedSteps) {
            val link = CareLink("link", "caregiver", "adult", status, null, null, false, null, null)
            val repository =
                object : CareLinkRepository {
                    override suspend fun acceptLink(caregiverId: String, code: String) =
                        AppResult.Success(link)

                    override suspend fun registerConsent(
                        careLinkId: String,
                        accepted: Boolean,
                    ): AppResult<CareLink> = error("Consent must not be submitted automatically")

                    override suspend fun getOlderAdult(olderAdultId: String) =
                        AppResult.Success(profile(olderAdultId))
                }
            val model =
                CareLinkViewModel(
                    "caregiver",
                    AcceptCareLinkCommandHandler(repository),
                    RegisterConsentCommandHandler(repository),
                    GetOlderAdultProfileQueryHandler(repository),
                )

            model.onCodeChange("TATA-4821")
            model.sendLinkRequest()

            assertEquals(status.name, expected, model.state.value.step)
            assertEquals(link, model.state.value.acceptedLink)
        }
    }
}
