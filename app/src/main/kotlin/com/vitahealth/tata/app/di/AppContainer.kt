package com.vitahealth.tata.app.di

import com.vitahealth.tata.carelink.application.handlers.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.handlers.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.infrastructure.remote.CareLinkApiService
import com.vitahealth.tata.carelink.infrastructure.remote.RemoteCareLinkRepository
import com.vitahealth.tata.carelink.presentation.link.CareLinkViewModel
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.handlers.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.handlers.VerifyCaregiverEmailCommandHandler
import com.vitahealth.tata.identity.infrastructure.remote.IdentityApiService
import com.vitahealth.tata.identity.infrastructure.remote.RemoteIdentityRepository
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(
    baseUrl: String = "http://10.0.2.2:8080/",
) {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val identityApi: IdentityApiService = retrofit.create(IdentityApiService::class.java)
    private val identityRepository = RemoteIdentityRepository(identityApi)

    private val careLinkApi: CareLinkApiService = retrofit.create(CareLinkApiService::class.java)
    private val careLinkRepository = RemoteCareLinkRepository(careLinkApi)

    val caregiverRegistrationViewModelFactory = CaregiverRegistrationViewModel.Factory(
        registerHandler = RegisterCaregiverCommandHandler(identityRepository),
        verifyHandler = VerifyCaregiverEmailCommandHandler(identityRepository),
        requestNewVerificationHandler = RequestNewVerificationCommandHandler(identityRepository),
    )

    fun careLinkViewModelFactory(caregiverId: String) = CareLinkViewModel.Factory(
        caregiverId = caregiverId,
        acceptCareLinkHandler = AcceptCareLinkCommandHandler(careLinkRepository),
        getOlderAdultHandler = GetOlderAdultProfileQueryHandler(careLinkRepository),
    )
}
