package com.vitahealth.tata.carelink.infrastructure.di

import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.commands.GenerateCareLinkCodeCommandHandler
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.application.commands.RegisterOlderAdultCommandHandler
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonEmergencyContactCommandHandler
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonProfileCommandHandler
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.queries.ListCaregiverProfilesQueryHandler
import com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedLinkedPersonEmergencyContactUpdater
import com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedLinkedPersonProfileUpdater
import com.vitahealth.tata.carelink.infrastructure.remote.CareLinkApiService
import com.vitahealth.tata.carelink.infrastructure.remote.CaregiverProfilesApiService
import com.vitahealth.tata.carelink.infrastructure.remote.RemoteCareLinkRepository
import com.vitahealth.tata.carelink.infrastructure.remote.RemoteCaregiverProfilesRepository
import com.vitahealth.tata.carelink.infrastructure.security.SessionScopedCaregiverProfilesRepository
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkViewModel
import com.vitahealth.tata.carelink.presentation.profiles.viewmodel.CaregiverProfilesViewModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import retrofit2.Retrofit

/** Composes linking, consent and caregiver profiles with the application's shared session. */
internal class CareLinkDependencies(
    retrofit: Retrofit,
    private val sessions: SessionStore,
    private val baseUrl: String,
) {
    private val careLinkApi: CareLinkApiService = retrofit.create(CareLinkApiService::class.java)
    private val careLinkRepository =
        RemoteCareLinkRepository(careLinkApi, sessions) { captured ->
            ApiClientFactory.create(baseUrl) { captured?.accessToken }
                .create(CareLinkApiService::class.java)
        }

    /** Session-scoped, owner-checked update of the linked adult's basic profile. */
    fun linkedPersonProfileUpdater(caregiverId: String) =
        UpdateLinkedPersonProfileCommandHandler(
            AuthorizedLinkedPersonProfileUpdater(sessions, caregiverId, baseUrl)
        )

    fun linkedPersonEmergencyContactUpdater(caregiverId: String) =
        UpdateLinkedPersonEmergencyContactCommandHandler(
            AuthorizedLinkedPersonEmergencyContactUpdater(sessions, caregiverId, baseUrl)
        )

    /** A consent-scoped list query for the application linked-person read projection. */
    fun linkedPersonLinksReader(caregiverId: String): ListCaregiverProfilesQueryHandler =
        ListCaregiverProfilesQueryHandler(
            SessionScopedCaregiverProfilesRepository(
                sessions,
                caregiverId,
                remoteFor = { captured ->
                    RemoteCaregiverProfilesRepository(
                        ApiClientFactory.create(baseUrl) { captured.accessToken }
                            .create(CaregiverProfilesApiService::class.java)
                    )
                },
            )
        )

    /** Creates profile operations bound to this caregiver with credentials fixed per request. */
    fun caregiverProfilesViewModelFactory(caregiverId: String): CaregiverProfilesViewModel.Factory {
        val repository =
            SessionScopedCaregiverProfilesRepository(
                sessions,
                caregiverId,
                remoteFor = { captured ->
                    val api =
                        ApiClientFactory.create(baseUrl) { captured.accessToken }
                            .create(CaregiverProfilesApiService::class.java)
                    RemoteCaregiverProfilesRepository(api)
                },
            )
        return CaregiverProfilesViewModel.Factory(
            caregiverId = caregiverId,
            listProfiles = ListCaregiverProfilesQueryHandler(repository),
            registerAdult = RegisterOlderAdultCommandHandler(repository),
            generateCode = GenerateCareLinkCodeCommandHandler(repository),
        )
    }

    /** Shares the linking adapter across request, consent and adult profile lookup. */
    fun careLinkViewModelFactory(caregiverId: String) =
        CareLinkViewModel.Factory(
            caregiverId = caregiverId,
            acceptCareLinkHandler = AcceptCareLinkCommandHandler(careLinkRepository),
            registerConsentHandler = RegisterConsentCommandHandler(careLinkRepository),
            getOlderAdultHandler = GetOlderAdultProfileQueryHandler(careLinkRepository),
        )
}
