package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/** Loads server-owned plans and subscriptions, preserving cancellation and transport failures. */
class RemoteSubscriptionRepository(private val api: SubscriptionApiService) :
    SubscriptionRepository {
    override suspend fun listPlans(): AppResult<List<Plan>> =
        request(call = { api.listPlans() }, map = { dtos -> dtos.map { it.toDomain() } }).let {
            result ->
            when (result) {
                is AppResult.Success ->
                    if (
                        result.value.any { it == null } ||
                            result.value.filterNotNull().map { it.code }.distinct().size !=
                                result.value.size
                    )
                        invalidResponse()
                    else AppResult.Success(result.value.filterNotNull())

                is AppResult.Failure -> result
            }
        }

    override suspend fun currentSubscription(accountId: String): AppResult<Subscription> =
        request(
                call = { api.currentSubscription(accountId) },
                map = { dto -> dto.toDomain()?.takeIf { it.accountId == accountId } },
            )
            .let { result ->
                when (result) {
                    is AppResult.Success ->
                        result.value?.let { AppResult.Success(it) } ?: invalidResponse()
                    is AppResult.Failure -> result
                }
            }

    override suspend fun changeSubscription(
        accountId: String,
        planCode: String,
    ): AppResult<Subscription> =
        request(
                call = { api.changeSubscription(accountId, ChangeSubscriptionRequest(planCode)) },
                map = { dto ->
                    dto.toDomain()?.takeIf { it.accountId == accountId && it.plan.code == planCode }
                },
            )
            .let { result ->
                when (result) {
                    is AppResult.Success ->
                        result.value?.let { AppResult.Success(it) } ?: invalidResponse()
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
            if (response.isSuccessful) {
                if (body == null) invalidResponse() else AppResult.Success(map(body))
            } else {
                AppResult.Failure(
                    message = "The subscription request failed.",
                    code = response.problemCode(),
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure(
                "Invalid subscription response.",
                exception,
                "INVALID_SUBSCRIPTION_RESPONSE",
            )
        } catch (exception: IOException) {
            AppResult.Failure(
                message = "No connection available.",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure(
                message = "Invalid subscription response.",
                cause = exception,
                code = "INVALID_SUBSCRIPTION_RESPONSE",
            )
        }

    private fun invalidResponse() =
        AppResult.Failure(
            message = "The service returned a plan this app does not recognise.",
            code = "INVALID_SUBSCRIPTION_RESPONSE",
        )
}
