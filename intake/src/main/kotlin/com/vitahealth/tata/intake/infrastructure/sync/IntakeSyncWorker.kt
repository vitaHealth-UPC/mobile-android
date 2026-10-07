package com.vitahealth.tata.intake.infrastructure.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitahealth.tata.intake.infrastructure.local.SQLiteIntakeLocalStore
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseConfirmationRepository
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IntakeSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val baseUrl = inputData.getString(KEY_BASE_URL)
            ?.takeIf { it.isNotBlank() }
            ?: return Result.failure()

        val local = SQLiteIntakeLocalStore(applicationContext)
        val sessions = com.vitahealth.tata.shared.infrastructure.security.EncryptedSessionStore(applicationContext)
        if (sessions.accessToken() == null) return Result.retry()
        val client = okhttp3.OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
            sessions.accessToken()?.let { request.header("Authorization", "Bearer $it") }
            chain.proceed(request.build())
        }.build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IntakeApiService::class.java)
        val remote = RemoteDoseConfirmationRepository(api)

        for (command in local.pendingConfirmations()) {
            when (val result = remote.confirm(command)) {
                is AppResult.Success -> {
                    local.cacheDose(result.value)
                    local.removePendingConfirmation(command.intakeId)
                }

                is AppResult.Failure -> {
                    when (result.code) {
                        "INTAKE_NOT_FOUND",
                        "INTAKE_NOT_CONFIRMABLE",
                        -> local.removePendingConfirmation(command.intakeId)

                        "NETWORK_UNAVAILABLE",
                        "REQUEST_FAILED",
                        null,
                        -> return Result.retry()

                        else -> return Result.retry()
                    }
                }
            }
        }

        return Result.success()
    }

    companion object {
        const val KEY_BASE_URL = "base_url"
        const val UNIQUE_WORK_NAME = "intake-pending-confirmations"
    }
}
