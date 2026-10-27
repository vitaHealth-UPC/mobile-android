package com.vitahealth.tata.shared.infrastructure.network

import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.lang.reflect.Type
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/** Separates HTTP body transfer failures from malformed content in a fully received JSON body. */
internal class JsonResponseBoundaryConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val delegate = retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return Converter<ResponseBody, Any> { body ->
            val contentType = body.contentType()
            // Reading the network body remains outside the conversion error boundary.
            val bytes = body.use { it.bytes() }
            try {
                bytes.toResponseBody(contentType).use { delegate.convert(it) }
            } catch (exception: IOException) {
                throw MalformedJsonException("Invalid JSON response", exception)
            }
        }
    }
}
