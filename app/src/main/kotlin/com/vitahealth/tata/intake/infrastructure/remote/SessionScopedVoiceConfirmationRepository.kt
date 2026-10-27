package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.ports.VoiceConfirmationRepository
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

/** Sends recorded audio with one captured session and rejects results after credentials change. */
class SessionScopedVoiceConfirmationRepository(
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> VoiceConfirmationRepository,
) : VoiceConfirmationRepository {
    override suspend fun confirm(
        command: ConfirmDoseByVoiceCommand
    ): AppResult<VoiceConfirmationReadModel> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || owner.role == "LINK_SETUP") {
            return AppResult.Failure(
                "Inicia sesión para confirmar la toma.",
                code = "SESSION_REQUIRED",
            )
        }
        val result = remoteFor(captured).confirm(command)
        val current = sessions.session()
        return if (current?.accessToken == captured.accessToken && current.owner == owner) result
        else
            AppResult.Failure(
                "La sesión cambió. Consulta el estado de la toma.",
                code = "SESSION_CHANGED",
            )
    }
}
