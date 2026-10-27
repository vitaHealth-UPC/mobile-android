package com.vitahealth.tata.app.integration.pushnavigation

import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQueryHandler
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Inter-context read orchestration: an Omission tap selects an existing,
 * authorized Monitoring alert without fabricating an alert or changing status.
 */
internal class PushAlertResolver(
    private val openAlerts: GetOpenAlertsQueryHandler,
) {
    suspend fun openAlertIdFor(destination: PushDestination.CaregiverAlerts): Long? =
        when (
            val result = openAlerts(
                GetOpenAlertsQuery(destination.caregiverId, destination.olderAdultId)
            )
        ) {
            is AppResult.Success ->
                alertForPush(result.value, destination.medicationName, destination.intakeId)?.id
            is AppResult.Failure -> null
        }
}
