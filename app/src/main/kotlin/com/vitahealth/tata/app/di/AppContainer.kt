package com.vitahealth.tata.app.di

import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
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

    val caregiverRegistrationViewModelFactory = CaregiverRegistrationViewModel.Factory(
        registerHandler = RegisterCaregiverCommandHandler(identityRepository),
        verifyHandler = VerifyCaregiverEmailCommandHandler(identityRepository),
    )
}
