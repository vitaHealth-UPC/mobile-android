package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.ContactRepository
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.domain.model.ContactOption
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * A missing channel (404) is a normal answer: the screen shows "Contacto no disponible". The name only
 * personalises the button, so a failed name read never blocks contact.
 */
class GetContactOptionQueryHandler(private val repository: ContactRepository) {
    suspend operator fun invoke(query: GetContactOptionQuery): AppResult<ContactOption> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) {
            return AppResult.Failure("No se pudo identificar a la persona.", code = AlertFailureCodes.INVALID_REFERENCE)
        }
        val olderAdultId = query.olderAdultId.trim()
        val channel = when (val result = repository.contactChannel(query.caregiverId.trim(), olderAdultId)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> if (result.code == AlertFailureCodes.NOT_FOUND) null else return result
        }
        val firstName = (repository.olderAdultFullName(olderAdultId) as? AppResult.Success)?.value
            ?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() }
        return AppResult.Success(ContactOption(channel, firstName))
    }
}
