package com.vitahealth.tata.carelink.application

import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommand
import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommand
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQuery
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks command/query boundaries without calling HTTP or device storage. */
class CareLinkCqrsHandlersTest {

    private val sample =
        CareLink("link", "caregiver", "adult",
            com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus.PENDING,
            null, null, false, null, null)
    private val adult =
        OlderAdultProfile("adult", "Rosa", LocalDate.of(1958, 5, 12), null, null, null)
    private val repository =
        object : CareLinkRepository {
            var accepts = 0
            var consentDecisions = 0
            var lookups = 0

            override suspend fun acceptLink(caregiverId: String, code: String): AppResult<CareLink> {
                accepts++
                return AppResult.Success(sample)
            }

            override suspend fun registerConsent(
                careLinkId: String,
                accepted: Boolean,
            ): AppResult<CareLink> {
                consentDecisions++
                return AppResult.Success(sample)
            }

            override suspend fun getOlderAdult(olderAdultId: String): AppResult<OlderAdultProfile> {
                lookups++
                return AppResult.Success(adult)
            }
        }

    @Test
    fun invalidReferencesCannotReachTheRepository() = runTest {
        assertTrue(
            AcceptCareLinkCommandHandler(repository)(AcceptCareLinkCommand("caregiver", " "))
                is AppResult.Failure
        )
        assertTrue(
            RegisterConsentCommandHandler(repository)(RegisterConsentCommand("", true))
                is AppResult.Failure
        )
        assertTrue(
            GetOlderAdultProfileQueryHandler(repository)(GetOlderAdultProfileQuery(""))
                is AppResult.Failure
        )
        assertEquals(0, repository.accepts)
        assertEquals(0, repository.consentDecisions)
        assertEquals(0, repository.lookups)
    }

    @Test
    fun validOperationsKeepReadAndWritePathsSeparate() = runTest {
        assertTrue(
            AcceptCareLinkCommandHandler(repository)(AcceptCareLinkCommand("caregiver", "ABC123"))
                is AppResult.Success
        )
        assertTrue(
            GetOlderAdultProfileQueryHandler(repository)(GetOlderAdultProfileQuery("adult"))
                is AppResult.Success
        )
        assertEquals(1, repository.accepts)
        assertEquals(1, repository.lookups)
        assertEquals(0, repository.consentDecisions)
    }
}
