package com.vitahealth.tata.shared.infrastructure.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/** Session credentials are encrypted with a non-exportable Android Keystore key. */
class EncryptedSessionStore(context: Context) : SessionStore, SessionObserver {
    private val preferences =
        context.applicationContext.getSharedPreferences("tata_session", Context.MODE_PRIVATE)

    /** Observes atomic preference commits without polling or notifying on each individual field. */
    override fun observeSessions(): Flow<StoredSession?> =
        callbackFlow<Unit> {
                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(Unit) }
                preferences.registerOnSharedPreferenceChangeListener(listener)
                trySend(Unit)
                awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            .conflate()
            .map { session() }
            .distinctUntilChanged { previous, current ->
                previous?.accessToken == current?.accessToken &&
                    previous?.owner == current?.owner &&
                    previous?.expiresAt == current?.expiresAt
            }

    @Synchronized
    override fun save(accessToken: String, expiresAt: String) {
        persist(accessToken, expiresAt, null)
    }

    /** Encrypts subject binding in the same payload as the credentials. */
    @Synchronized
    override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
        persist(accessToken, expiresAt, owner)
    }

    private fun persist(accessToken: String, expiresAt: String, owner: SessionOwner?) {
        require(accessToken.isNotBlank())
        require(Instant.parse(expiresAt).isAfter(Instant.now()))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val json = JSONObject().put("token", accessToken).put("expiry", expiresAt)
        owner?.let { json.put("subjectId", it.subjectId).put("role", it.role) }
        val payload = json.toString().toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(payload)
        check(
            preferences
                .edit()
                .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString("payload", Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .commit()
        ) {
            "Session could not be persisted"
        }
    }

    @Synchronized override fun accessToken(): String? = session()?.accessToken

    /** Reads old payloads without inventing an owner; a successful session lookup can bind them. */
    @Synchronized
    override fun session(): StoredSession? {
        val payload = preferences.getString("payload", null) ?: return null
        return try {
            val iv = preferences.getString("iv", null) ?: return clearAndReturnNull()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)),
            )
            val json =
                JSONObject(
                    String(cipher.doFinal(Base64.decode(payload, Base64.NO_WRAP)), Charsets.UTF_8)
                )
            val expiresAt = Instant.parse(json.getString("expiry"))
            if (!expiresAt.isAfter(Instant.now())) {
                clear()
                null
            } else {
                val owner =
                    if (json.has("subjectId") || json.has("role")) {
                        SessionOwner(json.getString("subjectId"), json.getString("role"))
                    } else null
                StoredSession(
                    json.getString("token").also { require(it.isNotBlank()) },
                    expiresAt,
                    owner,
                )
            }
        } catch (_: Exception) {
            clearAndReturnNull()
        }
    }

    @Synchronized
    override fun clear() {
        preferences.edit().clear().commit()
    }

    private fun clearAndReturnNull(): StoredSession? {
        clear()
        return null
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let {
            return it
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                        )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
            }
            .generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "tata_session_aes_v1"
    }
}
