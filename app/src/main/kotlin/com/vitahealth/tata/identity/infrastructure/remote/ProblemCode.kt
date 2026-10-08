package com.vitahealth.tata.identity.infrastructure.remote

import com.google.gson.JsonParser
import retrofit2.Response

/**
 * The backend answers errors as a problem detail whose title is the error code
 * (for example PLAN_NOT_FOUND). Falls back to the HTTP status when the body is not readable.
 */
internal fun Response<*>.problemCode(): String {
    val title = runCatching {
        JsonParser.parseString(errorBody()?.string().orEmpty()).asJsonObject.get("title")?.asString
    }.getOrNull()
    if (!title.isNullOrBlank()) return title
    return when (code()) {
        400 -> "VALIDATION_ERROR"
        401 -> "UNAUTHENTICATED"
        403 -> "ACCOUNT_NOT_ACTIVE"
        404 -> "ACCOUNT_NOT_FOUND"
        else -> "REQUEST_FAILED"
    }
}
