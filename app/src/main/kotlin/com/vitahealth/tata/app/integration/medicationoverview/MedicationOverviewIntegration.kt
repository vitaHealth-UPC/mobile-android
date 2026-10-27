package com.vitahealth.tata.app.integration.medicationoverview

import com.vitahealth.tata.intake.infrastructure.di.IntakeDependencies
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import com.vitahealth.tata.treatment.application.queries.GetMyMedicationsOverviewQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteMyMedicationsRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import com.vitahealth.tata.treatment.infrastructure.security.SessionScopedMyMedicationsOverviewReader
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MyMedicationsViewModel

/**
 * Integration adapter between Treatment's medication catalog and Intake's next
 * scheduled dose. Neither bounded context imports the other's implementation.
 */
internal class MedicationOverviewIntegration(
    private val sessions: SessionStore,
    private val baseUrl: String,
    private val intake: IntakeDependencies,
) {
    fun viewModelFactory(olderAdultId: String) =
        MyMedicationsViewModel.Factory(
            SessionScopedMyMedicationsOverviewReader(sessions, olderAdultId) { captured ->
                val client = ApiClientFactory.create(baseUrl) { captured.accessToken }
                val catalog =
                    RemoteMyMedicationsRepository(client.create(TreatmentApiService::class.java))
                val nextDose = intake.nextDoseFor(captured)

                GetMyMedicationsOverviewQueryHandler(catalog) {
                    when (val result = nextDose.getNextDose(olderAdultId)) {
                        is AppResult.Success ->
                            AppResult.Success(
                                result.value?.let {
                                    MedicationNextDose(it.id, it.medicationId, it.scheduledAt)
                                }
                            )
                        is AppResult.Failure -> result
                    }
                }
            }
        )
}
