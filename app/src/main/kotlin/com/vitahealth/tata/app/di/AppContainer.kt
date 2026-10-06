package com.vitahealth.tata.app.di

import com.vitahealth.tata.carelink.application.handlers.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.handlers.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.handlers.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.infrastructure.remote.CareLinkApiService
import com.vitahealth.tata.carelink.infrastructure.remote.RemoteCareLinkRepository
import com.vitahealth.tata.carelink.presentation.link.CareLinkViewModel
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.handlers.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.handlers.VerifyCaregiverEmailCommandHandler
import com.vitahealth.tata.identity.infrastructure.remote.IdentityApiService
import com.vitahealth.tata.identity.infrastructure.remote.RemoteIdentityRepository
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationViewModel
import com.vitahealth.tata.treatment.application.handlers.CreateTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.handlers.RegisterMedicationCommandHandler
import com.vitahealth.tata.treatment.application.handlers.SetDoseFrequencyCommandHandler
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationViewModel
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyViewModel
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

    private val treatmentApi: TreatmentApiService = retrofit.create(TreatmentApiService::class.java)
    private val treatmentRepository = RemoteTreatmentRepository(treatmentApi)

    val caregiverRegistrationViewModelFactory = CaregiverRegistrationViewModel.Factory(
        registerHandler = RegisterCaregiverCommandHandler(identityRepository),
        verifyHandler = VerifyCaregiverEmailCommandHandler(identityRepository),
        requestNewVerificationHandler = RequestNewVerificationCommandHandler(identityRepository),
    )

    fun careLinkViewModelFactory(caregiverId: String) = CareLinkViewModel.Factory(
        caregiverId = caregiverId,
        acceptCareLinkHandler = AcceptCareLinkCommandHandler(careLinkRepository),
        registerConsentHandler = RegisterConsentCommandHandler(careLinkRepository),
        getOlderAdultHandler = GetOlderAdultProfileQueryHandler(careLinkRepository),
    )

    fun medicationRegistrationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
    ) = MedicationRegistrationViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        registerMedicationHandler = RegisterMedicationCommandHandler(treatmentRepository),
    )

    fun treatmentCreationViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
    ) = TreatmentCreationViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        createTreatmentHandler = CreateTreatmentCommandHandler(treatmentRepository),
    )

    fun treatmentDoseFrequencyViewModelFactory(
        caregiverId: String,
        olderAdultId: String,
        olderAdultName: String,
        medicationId: String,
        medicationLabel: String,
        treatmentId: String,
        treatmentName: String,
    ) = TreatmentDoseFrequencyViewModel.Factory(
        caregiverId = caregiverId,
        olderAdultId = olderAdultId,
        olderAdultName = olderAdultName,
        medicationId = medicationId,
        medicationLabel = medicationLabel,
        treatmentId = treatmentId,
        treatmentName = treatmentName,
        handler = SetDoseFrequencyCommandHandler(),
    )
}
