package com.vitahealth.tata.intake.infrastructure.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitahealth.tata.intake.infrastructure.local.RoomIntakeLocalStore
import com.vitahealth.tata.intake.infrastructure.local.room.IntakeDatabase
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseConfirmationRepository
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import com.vitahealth.tata.shared.infrastructure.security.EncryptedSessionStore

/** Runs a single captured account's queue without switching credentials between commands. */
class IntakeSyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val baseUrl =
            inputData.getString(KEY_BASE_URL)?.takeIf { it.isNotBlank() } ?: return Result.failure()
        val sessions = EncryptedSessionStore(applicationContext)
        val captured = sessions.session() ?: return Result.success()
        val owner = captured.owner ?: return Result.success()
        if (owner.role == "LINK_SETUP") return Result.success()
        val local =
            RoomIntakeLocalStore(IntakeDatabase.getInstance(applicationContext).intakes(), owner)
        val api =
            ApiClientFactory.create(baseUrl) { captured.accessToken }
                .create(IntakeApiService::class.java)
        val replay =
            ReplayPendingConfirmations(RemoteDoseConfirmationRepository(api), local) {
                val current = sessions.session()
                current?.accessToken == captured.accessToken && current.owner == owner
            }
        return when (replay.replay()) {
            ConfirmationReplayResult.COMPLETE,
            ConfirmationReplayResult.AUTHORIZATION_REQUIRED,
            ConfirmationReplayResult.SESSION_CHANGED -> Result.success()
            ConfirmationReplayResult.RETRY -> Result.retry()
        }
    }

    companion object {
        const val KEY_BASE_URL = "base_url"
        const val UNIQUE_WORK_NAME = "intake-pending-confirmations"
    }
}
