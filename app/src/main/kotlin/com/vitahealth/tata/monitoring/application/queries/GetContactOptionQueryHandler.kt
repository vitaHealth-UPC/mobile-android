package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.domain.repositories.ContactRepository
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * A missing channel (404) is a normal answer: the screen shows "Contacto no disponible". The name
 * only personalises the button; temporary name lookup failures do not block contact. Authorization
 * and invalid response failures are propagated instead of exposing a previously fetched channel.
 */
class GetContactOptionQueryHandler(private val repository: ContactRepository) :
    ContactOptionReader {
    override suspend operator fun invoke(query: GetContactOptionQuery): AppResult<ContactOption> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) {
            return AppResult.Failure(
                "No se pudo identificar a la persona.",
                code = AlertFailureCodes.INVALID_REFERENCE,
            )
        }
        val olderAdultId = query.olderAdultId.trim()
        val channel =
            when (val result = repository.contactChannel(query.caregiverId.trim(), olderAdultId)) {
                is AppResult.Success -> result.value
                is AppResult.Failure ->
                    if (result.code == AlertFailureCodes.NOT_FOUND) null else return result
            }
        currentCoroutineContext().ensureActive()
        val fullName =
            when (val result = repository.olderAdultFullName(olderAdultId)) {
                is AppResult.Success -> result.value
                is AppResult.Failure ->
                    when (result.code) {
                        AlertFailureCodes.NETWORK,
                        AlertFailureCodes.NOT_FOUND,
                        AlertFailureCodes.REQUEST_FAILED -> null
                        else -> return result
                    }
            }
        currentCoroutineContext().ensureActive()
        val firstName = fullName?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() }
        return AppResult.Success(ContactOption(channel, firstName))
    }
}
