package com.vitahealth.tata.omission.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

/** Authenticated installation endpoints; ownership is resolved by the backend from the bearer. */
interface PushDeviceApiService {
    @PUT("api/v1/push-devices/{installationId}")
    suspend fun register(
        @Path("installationId") installationId: String,
        @Body request: RegisterPushDeviceRequest,
    ): Response<PushDeviceResponse>

    @DELETE("api/v1/push-devices/{installationId}")
    suspend fun unregister(@Path("installationId") installationId: String): Response<Unit>
}
