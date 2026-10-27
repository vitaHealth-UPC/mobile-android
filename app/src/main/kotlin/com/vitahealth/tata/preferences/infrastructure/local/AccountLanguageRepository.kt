package com.vitahealth.tata.preferences.infrastructure.local

import android.content.Context
import com.vitahealth.tata.preferences.application.ports.ApplicationLanguageRepository
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import java.security.MessageDigest
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/** A language preference per verified account; no clinical values or tokens are persisted. */
class AccountLanguageRepository(
    context: Context,
    private val sessions: SessionStore,
    private val observer: SessionObserver,
    private val now: () -> Instant = Instant::now,
) : ApplicationLanguageRepository {
    private val preferences =
        context.applicationContext.getSharedPreferences("tata_app_locale", Context.MODE_PRIVATE)
    private val revision = MutableStateFlow(0)

    override fun observe() = combine(observer.observeSessions(), revision) { session, _ ->
        val owner = session?.owner
        if (owner == null || !session.expiresAt.isAfter(now())) AppLanguage.SPANISH
        else AppLanguage.fromTag(preferences.getString(key(owner.storageKey), null))
    }.distinctUntilChanged()

    override suspend fun change(userId: String, language: AppLanguage): AppResult<AppLanguage> {
        val captured = sessions.session()
            ?: return AppResult.Failure("Sesión requerida.", code = "AUTHENTICATION_REQUIRED")
        val owner = captured.owner
        if (owner == null || !captured.expiresAt.isAfter(now()))
            return AppResult.Failure("Sesión caducada.", code = "AUTHENTICATION_REQUIRED")
        if (owner.role !in setOf("CAREGIVER", "OLDER_ADULT") || owner.subjectId != userId)
            return AppResult.Failure("Acceso denegado.", code = "RESOURCE_ACCESS_DENIED")
        return synchronized(sessions) {
            if (!sessions.isCurrent(captured) || !captured.expiresAt.isAfter(now())) {
                AppResult.Failure("La sesión cambió.", code = "SESSION_CHANGED")
            } else if (!preferences.edit().putString(key(owner.storageKey), language.tag).commit()) {
                AppResult.Failure("No se pudo guardar el idioma.", code = "LOCAL_WRITE_FAILED")
            } else {
                revision.value = revision.value + 1
                AppResult.Success(language)
            }
        }
    }

    companion object {
        /** Used before Activity attachment so Android resources use the correct account locale. */
        fun languageFor(context: Context, ownerStorageKey: String?): AppLanguage {
            if (ownerStorageKey == null) return AppLanguage.SPANISH
            val settings = context.applicationContext.getSharedPreferences(
                "tata_app_locale", Context.MODE_PRIVATE,
            )
            return AppLanguage.fromTag(settings.getString(key(ownerStorageKey), null))
        }

        private fun key(owner: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(("tata:locale:" + owner).toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
