package com.vitahealth.tata.monitoring.presentation.contact

import com.vitahealth.tata.monitoring.FakeAlertsRepository
import com.vitahealth.tata.monitoring.FakeContactRepository
import com.vitahealth.tata.monitoring.FakeNotesRepository
import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.failure
import com.vitahealth.tata.monitoring.infrastructure.remote.ContactResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.FamilyMonitoringApiService
import com.vitahealth.tata.monitoring.infrastructure.remote.OlderAdultProfileResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.RemoteContactRepository
import com.vitahealth.tata.monitoring.infrastructure.remote.toDomain
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response

/** US-29: contact the older adult from an alert. */
class ContactTest {
    private val contacts = FakeContactRepository()

    private fun api(answer: (String) -> Any): FamilyMonitoringApiService =
        Proxy.newProxyInstance(
            FamilyMonitoringApiService::class.java.classLoader,
            arrayOf(FamilyMonitoringApiService::class.java),
        ) { _, method, _ ->
            answer(method.name)
        } as FamilyMonitoringApiService

    private fun <T> httpError(code: Int): Response<T> =
        Response.error(code, """{"code":"X","message":"m"}""".toResponseBody())

    private fun detailModel(): AlertDetailViewModel {
        val alerts = FakeAlertsRepository().apply { detail = AppResult.Success(alert(id = 1)) }
        return AlertDetailViewModel(
            "caregiver",
            "adult",
            1,
            GetAlertDetailQueryHandler(alerts),
            UpdateAlertStatusCommandHandler(alerts),
            RegisterFollowUpNoteCommandHandler(FakeNotesRepository()),
            GetContactOptionQueryHandler(contacts),
            Dispatchers.Unconfined,
        )
    }

    @Test
    fun revokedAccessDuringNameLookupDoesNotExposeTheChannel() = runBlocking {
        for (code in
            listOf(
                AlertFailureCodes.ACCESS_DENIED,
                AlertFailureCodes.SESSION_REQUIRED,
                AlertFailureCodes.INVALID_RESPONSE,
            )) {
            contacts.fullName = failure(code)
            val result = GetContactOptionQueryHandler(contacts)(GetContactOptionQuery("c", "a"))
            assertEquals(code, (result as AppResult.Failure).code)
        }
    }

    @Test
    fun remoteNameMustBelongToTheRequestedAdult() = runBlocking {
        val repository =
            RemoteContactRepository(
                api { Response.success(OlderAdultProfileResponse("other", "Rosa")) }
            )
        assertEquals(
            AlertFailureCodes.INVALID_RESPONSE,
            (repository.olderAdultFullName("adult") as AppResult.Failure).code,
        )
    }

    @Test
    fun unauthorizedNameLookupHasAnExplicitSessionFailure() = runBlocking {
        val repository = RemoteContactRepository(api { httpError<OlderAdultProfileResponse>(401) })
        assertEquals(
            AlertFailureCodes.SESSION_REQUIRED,
            (repository.olderAdultFullName("adult") as AppResult.Failure).code,
        )
    }

    @Test
    fun whatsappLinksUseOnlyTheDigits() {
        assertEquals(
            "51999888777",
            ContactChannel(ContactChannelType.WHATSAPP, "+51 999-888 777").digits,
        )
    }

    @Test
    fun mapsBothSwaggerChannelTypes() {
        assertEquals(
            ContactChannel(ContactChannelType.PHONE, "+51 999 888 777"),
            ContactResponse("PHONE", "+51 999 888 777").toDomain(),
        )
        assertEquals(
            ContactChannelType.WHATSAPP,
            ContactResponse("WHATSAPP", "+51 999 888 777").toDomain().type,
        )
    }

    @Test
    fun readsTheChannelAndTheName() = runBlocking {
        val repository =
            RemoteContactRepository(
                api { method ->
                    when (method) {
                        "contact" ->
                            Response.success(ContactResponse("WHATSAPP", "+51 999 888 777"))
                        "profile" ->
                            Response.success(OlderAdultProfileResponse("adult", "Rosa Vargas"))
                        else -> error("Unexpected request: $method")
                    }
                }
            )

        assertEquals(
            ContactChannelType.WHATSAPP,
            (repository.contactChannel("c", "a") as AppResult.Success).value.type,
        )
        assertEquals(
            "Rosa Vargas",
            (repository.olderAdultFullName("adult") as AppResult.Success).value,
        )
    }

    @Test
    fun mapsMissingAndInvalidChannels() = runBlocking {
        val missing =
            RemoteContactRepository(api { httpError<ContactResponse>(404) })
                .contactChannel("c", "a")
        val unknown =
            RemoteContactRepository(api { Response.success(ContactResponse("EMAIL", "a@b.c")) })
                .contactChannel("c", "a")

        assertEquals(AlertFailureCodes.NOT_FOUND, (missing as AppResult.Failure).code)
        assertEquals(AlertFailureCodes.INVALID_RESPONSE, (unknown as AppResult.Failure).code)
    }

    @Test
    fun aMissingChannelIsAnUnavailableContact() = runBlocking {
        contacts.channel = failure(AlertFailureCodes.NOT_FOUND)

        val option =
            (GetContactOptionQueryHandler(contacts)(GetContactOptionQuery("c", "a"))
                    as AppResult.Success)
                .value

        assertNull(option.channel)
        assertEquals("Rosa", option.firstName)
    }

    @Test
    fun aFailedNameReadStillOffersContact() = runBlocking {
        contacts.fullName = failure(AlertFailureCodes.NETWORK)

        val option =
            (GetContactOptionQueryHandler(contacts)(GetContactOptionQuery("c", "a"))
                    as AppResult.Success)
                .value

        assertEquals(ContactChannelType.PHONE, option.channel!!.type)
        assertNull(option.firstName)
    }

    @Test
    fun aNetworkFailureOnTheChannelIsReported() = runBlocking {
        contacts.channel = failure(AlertFailureCodes.NETWORK)

        val result = GetContactOptionQueryHandler(contacts)(GetContactOptionQuery("c", "a"))

        assertEquals(AlertFailureCodes.NETWORK, (result as AppResult.Failure).code)
    }

    @Test
    fun theAlertDetailOffersContactWithTheFirstName() {
        val state = detailModel().contact.value

        val option = (state as ContactUiState.Ready).option
        assertEquals(
            ContactOption(ContactChannel(ContactChannelType.PHONE, "+51 999 888 777"), "Rosa"),
            option,
        )
        assertEquals(listOf("contact:caregiver:adult", "profile:adult"), contacts.requests)
    }

    @Test
    fun theAlertDetailShowsUnavailableContactAndCanAskAgain() {
        contacts.channel = failure(AlertFailureCodes.NETWORK)
        val model = detailModel()
        assertEquals(ContactUiState.Failed(AlertsProblem.NETWORK), model.contact.value)

        contacts.channel = failure(AlertFailureCodes.NOT_FOUND)
        model.loadContact()

        assertNull((model.contact.value as ContactUiState.Ready).option.channel)
    }
}
