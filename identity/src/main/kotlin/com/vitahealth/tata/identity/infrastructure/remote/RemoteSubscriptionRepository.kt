package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.application.SubscriptionRepository
import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.shared.common.result.AppResult
import retrofit2.Response

class RemoteSubscriptionRepository(
    private val api: SubscriptionApiService,
) : SubscriptionRepository {
    override suspend fun listPlans(): AppResult<List<Plan>> =
        request(
            call = { api.listPlans() },
            map = { dtos -> dtos.map { it.toDomain() } },
        ).let { result ->
            when (result) {
                is AppResult.Success ->
                    if (result.value.any { it == null }) invalidResponse() else AppResult.Success(result.value.filterNotNull())

                is AppResult.Failure -> result
            }
        }

    override suspend fun currentSubscription(accountId: String): AppResult<Subscription> =
        request(
            call = { api.currentSubscription(accountId) },
            map = { it.toDomain() },
        ).let { result ->
            when (result) {
                is AppResult.Success -> result.value?.let { AppResult.Success(it) } ?: invalidResponse()
                is AppResult.Failure -> result
            }
        }

    private suspend fun <Dto, Out> request(
        call: suspend () -> Response<Dto>,
        map: (Dto) -> Out,
    ): AppResult<Out> =
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AppResult.Success(map(body))
            } else {
                AppResult.Failure(message = "The subscription request failed.", code = response.problemCode())
            }
        } catch (exception: Exception) {
            AppResult.Failure(message = "No connection available.", cause = exception, code = "NETWORK_UNAVAILABLE")
        }

    private fun invalidResponse() = AppResult.Failure(
        message = "The service returned a plan this app does not recognise.",
        code = "INVALID_SUBSCRIPTION_RESPONSE",
    )
}
