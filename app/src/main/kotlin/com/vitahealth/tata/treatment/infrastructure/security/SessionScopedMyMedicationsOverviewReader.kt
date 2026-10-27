package com.vitahealth.tata.treatment.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsOverviewReader
import com.vitahealth.tata.treatment.application.readmodels.MyMedicationsOverview
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Captures one adult session for the entire catalog and next-intake operation. */
class SessionScopedMyMedicationsOverviewReader(
    private val sessions: SessionStore,
    private val olderAdultId: String,
    private val readerFor: (StoredSession) -> MyMedicationsOverviewReader,
) : MyMedicationsOverviewReader {
    override suspend fun read(): AppResult<MyMedicationsOverview> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || !captured.expiresAt.isAfter(Instant.now())) {
            return AppResult.Failure(
                "Inicia sesión para consultar tus medicamentos.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner.role != "OLDER_ADULT" || owner.subjectId != olderAdultId) {
            return AppResult.Failure(
                "La cuenta no corresponde al adulto solicitado.",
                code = "OLDER_ADULT_SESSION_REQUIRED",
            )
        }
        val result = readerFor(captured).read()
        currentCoroutineContext().ensureActive()
        val current = sessions.session()
        if (
            current?.accessToken != captured.accessToken ||
                current.owner != owner ||
                !current.expiresAt.isAfter(Instant.now())
        ) {
            return AppResult.Failure(
                "La sesión cambió. Abre nuevamente tus medicamentos.",
                code = "SESSION_CHANGED",
            )
        }
        return result
    }
}
