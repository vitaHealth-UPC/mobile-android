package com.vitahealth.tata.preferences.infrastructure.remote

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

class PreferencesRequestTest {
    @Test
    fun cancellationIsPropagatedToTheCaller() = runBlocking {
        val expected = CancellationException("screen closed")
        try {
            preferencesRequest("user") { throw expected }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(expected, actual)
        }
    }

    @Test
    fun onlyTransportFailureIsClassifiedAsOffline() = runBlocking {
        assertEquals(
            "NETWORK_UNAVAILABLE",
            (preferencesRequest("user") { throw IOException("offline") } as AppResult.Failure).code,
        )
        for (failure in
            listOf(
                MalformedJsonException("truncated JSON"),
                JsonSyntaxException("wrong type"),
                IllegalArgumentException("invalid fields"),
            )) {
            assertEquals(
                "INVALID_PREFERENCES_RESPONSE",
                (preferencesRequest("user") { throw failure } as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun authorizationAndValidationKeepTheirOwnCodes() = runBlocking {
        for ((http, code) in
            listOf(
                400 to "REQUEST_VALIDATION_FAILED",
                401 to "SESSION_REQUIRED",
                403 to "PREFERENCES_ACCESS_DENIED",
                503 to "REQUEST_FAILED",
            )) {
            val result = preferencesRequest("user") { Response.error(http, "{}".toResponseBody()) }
            assertEquals(code, (result as AppResult.Failure).code)
        }
    }

    @Test
    fun successfulResponseWithoutBodyIsInvalid() = runBlocking {
        val result =
            preferencesRequest("user") { Response.success<UserPreferencesResponse>(204, null) }
        assertEquals("INVALID_PREFERENCES_RESPONSE", (result as AppResult.Failure).code)
    }
}
