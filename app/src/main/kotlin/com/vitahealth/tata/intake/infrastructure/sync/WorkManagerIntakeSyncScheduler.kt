package com.vitahealth.tata.intake.infrastructure.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.vitahealth.tata.shared.sync.SyncScheduler
import java.util.concurrent.TimeUnit

class WorkManagerIntakeSyncScheduler(
    context: Context,
    private val baseUrl: String,
) : SyncScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<IntakeSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                workDataOf(IntakeSyncWorker.KEY_BASE_URL to baseUrl),
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS,
            )
            .build()

        workManager.enqueueUniqueWork(
            IntakeSyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
