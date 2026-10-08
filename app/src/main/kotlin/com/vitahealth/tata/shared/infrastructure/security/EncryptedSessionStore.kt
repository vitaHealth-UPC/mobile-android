package com.vitahealth.tata.shared.infrastructure.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.vitahealth.tata.shared.application.SessionStore
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONObject

/** Session credentials are encrypted with a non-exportable Android Keystore key. */
class EncryptedSessionStore(context: Context) : SessionStore {
    private val preferences = context.applicationContext.getSharedPreferences("tata_session", Context.MODE_PRIVATE)

    @Synchronized
    override fun save(accessToken: String, expiresAt: String) {
        require(accessToken.isNotBlank())
        require(Instant.parse(expiresAt).isAfter(Instant.now()))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val payload = JSONObject().put("token", accessToken).put("expiry", expiresAt).toString().toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(payload)
        check(preferences.edit()
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("payload", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .commit()) { "Session could not be persisted" }
    }

    @Synchronized
    override fun accessToken(): String? {
        val payload = preferences.getString("payload", null) ?: return null
        return try {
            val iv = preferences.getString("iv", null) ?: return clearAndReturnNull()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
            val json = JSONObject(String(cipher.doFinal(Base64.decode(payload, Base64.NO_WRAP)), Charsets.UTF_8))
            if (Instant.parse(json.getString("expiry")).isAfter(Instant.now())) json.getString("token")
            else clearAndReturnNull()
        } catch (_: Exception) {
            clearAndReturnNull()
        }
    }

    @Synchronized
    override fun clear() { preferences.edit().clear().commit() }

    private fun clearAndReturnNull(): String? { clear(); return null }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
        }.generateKey()
    }

    private companion object { const val KEY_ALIAS = "tata_session_aes_v1" }
}
