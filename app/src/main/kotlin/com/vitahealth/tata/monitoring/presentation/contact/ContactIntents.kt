package com.vitahealth.tata.monitoring.presentation.contact

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.vitahealth.tata.monitoring.domain.model.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.ContactChannelType

/**
 * Phone opens the dialer with the number already typed (ACTION_DIAL needs no CALL_PHONE permission);
 * WhatsApp opens a wa.me link, which the WhatsApp app or the browser handles.
 */
internal fun ContactChannel.toIntent(): Intent = when (type) {
    ContactChannelType.PHONE -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", value, null))
    ContactChannelType.WHATSAPP -> Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
}

/**
 * Starts the contact app. It tries the launch instead of asking `resolveActivity` first, because Android 11+
 * hides other apps from that query unless the manifest declares them. Returns false when nothing can open it.
 */
internal fun Context.openContact(channel: ContactChannel): Boolean = try {
    startActivity(channel.toIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
}
