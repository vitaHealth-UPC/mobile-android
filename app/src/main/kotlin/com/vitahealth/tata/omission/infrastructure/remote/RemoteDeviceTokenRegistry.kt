package com.vitahealth.tata.omission.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.omission.application.ports.DeviceTokenRegistry
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Registers one installation with fixed operation credentials and validates its server receipt. */
class RemoteDeviceTokenRegistry(
    private val sessions: SessionStore,
    private val installationId: String,
    private val apiFor: (StoredSession) -> PushDeviceApiService,
    private val now: () -> Instant = Instant::now,
) : DeviceTokenRegistry {
    init {
        require(UUID.fromString(installationId).toString() == installationId)
    }

    override suspend fun register(token: String, expectedSession: StoredSession?): AppResult<Unit> {
        if (token.isBlank() || token.length > 4096) {
            return AppResult.Failure(
                "El token de dispositivo no es válido.",
                code = "INVALID_DEVICE_TOKEN",
            )
        }
        return withSession(expectedSession) { captured ->
            val response =
                apiFor(captured).register(installationId, RegisterPushDeviceRequest(token))
            if (!response.isSuccessful) return@withSession httpFailure(response.code())
            val receipt = requireNotNull(response.body())
            require(receipt.installationId == installationId)
            Instant.parse(receipt.updatedAt)
            AppResult.Success(Unit)
        }
    }

    /** Removes this installation using the still-authenticated session before local sign-out. */
    override suspend fun unregister(expectedSession: StoredSession?): AppResult<Unit> =
        withSession(expectedSession) { captured ->
            val response = apiFor(captured).unregister(installationId)
            if (response.code() == 204) AppResult.Success(Unit)
            else if (response.isSuccessful)
                AppResult.Failure("Respuesta de baja inesperada.", code = "INVALID_RESPONSE")
            else httpFailure(response.code())
        }

    private suspend fun withSession(
        expectedSession: StoredSession?,
        operation: suspend (StoredSession) -> AppResult<Unit>,
    ): AppResult<Unit> {
        val captured = sessions.session()
        if (
            captured?.owner == null ||
                captured.owner.role == "LINK_SETUP" ||
                !captured.expiresAt.isAfter(now())
        ) {
            return AppResult.Failure(
                "Inicia sesión para registrar el dispositivo.",
                code = "SESSION_REQUIRED",
            )
        }
        if (
            expectedSession != null &&
                (captured.owner != expectedSession.owner ||
                    captured.accessToken != expectedSession.accessToken)
        ) {
            return AppResult.Failure(
                "La sesión cambió antes del registro.",
                code = "SESSION_CHANGED",
            )
        }
        return try {
            val result = operation(captured)
            currentCoroutineContext().ensureActive()
            val current = sessions.session()
            if (
                current?.owner != captured.owner ||
                    current.accessToken != captured.accessToken ||
                    !current.expiresAt.isAfter(now())
            ) {
                AppResult.Failure("La sesión cambió durante el registro.", code = "SESSION_CHANGED")
            } else result
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Respuesta de dispositivo inválida.", code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure("No hay conexión disponible.", code = "NETWORK_UNAVAILABLE")
        } catch (exception: RuntimeException) {
            AppResult.Failure("Respuesta de dispositivo inválida.", code = "INVALID_RESPONSE")
        }
    }

    private fun httpFailure(status: Int): AppResult.Failure =
        AppResult.Failure(
            "No se pudo actualizar el dispositivo.",
            code =
                when (status) {
                    400 -> "INVALID_DEVICE_TOKEN"
                    401 -> "SESSION_REQUIRED"
                    403 -> "ACCESS_DENIED"
                    else -> "REQUEST_FAILED"
                },
        )
}
