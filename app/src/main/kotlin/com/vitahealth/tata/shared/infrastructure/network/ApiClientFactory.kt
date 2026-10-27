package com.vitahealth.tata.shared.infrastructure.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Builds foreground and background API clients with the same transport policy. */
object ApiClientFactory {
    private val transport: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .callTimeout(110, TimeUnit.SECONDS)
            .build()
    }

    /** Reads credentials for each request so login, refresh and logout affect existing clients. */
    fun create(baseUrl: String, accessToken: () -> String?): Retrofit {
        val client =
            transport
                .newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                    accessToken()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { token -> request.header("Authorization", "Bearer $token") }
                    chain.proceed(request.build())
                }
                .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JsonResponseBoundaryConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
