package com.vitahealth.tata.identity.infrastructure.external

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.net.URI

/**
 * Only explicit HTTPS, mailto and E.164 phone destinations from deployment configuration are
 * eligible for the public landing. It never accepts an intent URI, app command or local file.
 */
class PublicContactDestination private constructor(
    val value: String,
    val kind: Kind,
) {
    enum class Kind { WEB, EMAIL, PHONE }

    companion object {
        fun parse(configured: String?): PublicContactDestination? {
            val value = configured?.trim().orEmpty()
            if (value.isBlank() || value.length > 1024) return null
            val uri = try { URI(value) }
            catch (_: java.net.URISyntaxException) { return null }
            catch (_: IllegalArgumentException) { return null }
            if (uri.fragment != null) return null
            val kind = when (uri.scheme?.lowercase()) {
                "https" -> {
                    if (uri.host.isNullOrBlank() || !uri.host.contains('.') ||
                        uri.userInfo != null || uri.port !in setOf(-1, 443)) return null
                    Kind.WEB
                }
                "mailto" -> {
                    if (uri.rawSchemeSpecificPart?.matches(
                        Regex("[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
                    ) != true) return null
                    Kind.EMAIL
                }
                "tel" -> {
                    if (uri.rawSchemeSpecificPart?.matches(
                        Regex("\\+[1-9][0-9]{7,14}")
                    ) != true) return null
                    Kind.PHONE
                }
                else -> return null
            }
            return PublicContactDestination(value, kind)
        }
    }
}

/**
 * Platform-only adapter: presentation receives a callback, not Android Context or URI intent
 * resolution. A missing or failed destination degrades to the unavailable Figma state.
 */
@Composable
fun rememberPublicContactAction(configured: String): (() -> Unit)? {
    val context = LocalContext.current
    val destination = remember(configured) { PublicContactDestination.parse(configured) }
    var available by remember(destination) { mutableStateOf(destination != null) }
    return if (destination != null && available) {
        { if (!PublicContactLauncher.open(context, destination)) available = false }
    } else null
}

/** Only responds to an explicit user tap; no automatic opening during composition or startup. */
object PublicContactLauncher {
    fun open(context: Context, target: PublicContactDestination): Boolean {
        val action = when (target.kind) {
            PublicContactDestination.Kind.WEB -> Intent.ACTION_VIEW
            PublicContactDestination.Kind.EMAIL -> Intent.ACTION_SENDTO
            PublicContactDestination.Kind.PHONE -> Intent.ACTION_DIAL
        }
        val intent = Intent(action, Uri.parse(target.value))
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
