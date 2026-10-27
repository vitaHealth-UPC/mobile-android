package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.ports.DoseDetailRepository
import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.ports.NextDoseRepository
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseConfirmationRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseDetailRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteIntakeAgendaRepository
import com.vitahealth.tata.intake.infrastructure.remote.RemoteNextDoseRepository
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.intake.application.ports.SyncScheduler
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Captures one credential and matching local partition for each foreground intake operation. */
class SessionScopedIntakeRepositories(
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> IntakeApiService,
    private val localFor: (SessionOwner) -> IntakeLocalStore,
    private val scheduler: SyncScheduler,
    private val now: () -> Instant = Instant::now,
) : NextDoseRepository, IntakeAgendaRepository, DoseDetailRepository, DoseConfirmationRepository {

    override suspend fun getNextDose(olderAdultId: String) =
        scoped(olderAdultId) { api, local ->
            OfflineFirstNextDoseRepository(RemoteNextDoseRepository(api), local)
                .getNextDose(olderAdultId)
        }

    override suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant) =
        scoped(olderAdultId) { api, local ->
            OfflineFirstIntakeAgendaRepository(RemoteIntakeAgendaRepository(api), local)
                .getAgenda(olderAdultId, from, to)
        }

    override suspend fun getDoseDetail(intakeId: String) = scoped { api, local ->
        OfflineFirstDoseDetailRepository(RemoteDoseDetailRepository(api), local)
            .getDoseDetail(intakeId)
    }

    override suspend fun confirm(command: ConfirmDoseCommand) = scoped { api, local ->
        OfflineFirstDoseConfirmationRepository(
                RemoteDoseConfirmationRepository(api),
                local,
                scheduler,
            )
            .confirm(command)
    }

    private suspend fun <T> scoped(
        olderAdultId: String? = null,
        operation: suspend (IntakeApiService, IntakeLocalStore) -> AppResult<T>,
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (
            captured == null ||
                owner == null ||
                !captured.expiresAt.isAfter(now()) ||
                owner.role !in setOf("OLDER_ADULT", "CAREGIVER")
        ) {
            return AppResult.Failure(
                "Inicia sesión para consultar tus tomas.",
                code = "SESSION_REQUIRED",
            )
        }
        if (
            olderAdultId != null && owner.role == "OLDER_ADULT" && owner.subjectId != olderAdultId
        ) {
            return AppResult.Failure(
                "La cuenta no corresponde al adulto mayor solicitado.",
                code = "OLDER_ADULT_SESSION_REQUIRED",
            )
        }
        val result = operation(remoteFor(captured), localFor(owner))
        currentCoroutineContext().ensureActive()
        val current = sessions.session()
        return if (
            current?.accessToken == captured.accessToken &&
                current.owner == owner &&
                current.expiresAt.isAfter(now())
        )
            result
        else
            AppResult.Failure(
                "La sesión cambió. Vuelve a abrir tus tomas.",
                code = "SESSION_CHANGED",
            )
    }
}
