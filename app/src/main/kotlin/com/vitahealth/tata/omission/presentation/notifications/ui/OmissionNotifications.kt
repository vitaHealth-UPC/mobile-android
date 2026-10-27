package com.vitahealth.tata.omission.presentation.notifications.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vitahealth.tata.R
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import com.vitahealth.tata.omission.presentation.notifications.mappers.OmissionPushIntent

/**
 * Shows the reinforced reminder and the caregiver alert in the user's language. The backend writes
 * its texts in English, so the device builds its own from the notification kind and the medication
 * name.
 */
object OmissionNotifications {
    /** Removes this feature's notices while retaining unrelated application notifications. */
    fun clearDisplayed(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.activeNotifications
            .filter {
                it.notification.channelId == REMINDERS_CHANNEL ||
                    it.notification.channelId == CAREGIVER_ALERTS_CHANNEL
            }
            .forEach { manager.cancel(it.tag, it.id) }
    }

    const val REMINDERS_CHANNEL = "tata_reinforced_reminders"
    const val CAREGIVER_ALERTS_CHANNEL = "tata_caregiver_alerts"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                    REMINDERS_CHANNEL,
                    context.getString(R.string.omission_channel_reminders),
                    NotificationManager.IMPORTANCE_HIGH,
                )
                .apply {
                    description = context.getString(R.string.omission_channel_reminders_description)
                }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                    CAREGIVER_ALERTS_CHANNEL,
                    context.getString(R.string.omission_channel_alerts),
                    NotificationManager.IMPORTANCE_HIGH,
                )
                .apply {
                    description = context.getString(R.string.omission_channel_alerts_description)
                }
        )
    }

    /** Does nothing when the user did not allow notifications (Android 13+). */
    fun show(context: Context, push: OmissionPush) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
        )
            return
        ensureChannels(context)
        val reminder = push.kind == OmissionPushKind.REINFORCED_REMINDER
        val title =
            context.getString(
                if (reminder) R.string.omission_reminder_title else R.string.omission_alert_title
            )
        val text =
            when {
                reminder && push.medicationName != null ->
                    context.getString(R.string.omission_reminder_text_named, push.medicationName)
                reminder -> context.getString(R.string.omission_reminder_text)
                push.medicationName != null ->
                    context.getString(R.string.omission_alert_text_named, push.medicationName)
                else -> context.getString(R.string.omission_alert_text)
            }
        val notification =
            NotificationCompat.Builder(
                    context,
                    if (reminder) REMINDERS_CHANNEL else CAREGIVER_ALERTS_CHANNEL,
                )
                .setSmallIcon(R.drawable.ic_omission_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(
                    if (reminder) NotificationCompat.CATEGORY_REMINDER
                    else NotificationCompat.CATEGORY_ALARM
                )
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .apply { openIntent(context, push)?.let(::setContentIntent) }
                .build()
        NotificationManagerCompat.from(context).notify(notificationId(push), notification)
    }

    /**
     * Delivery retries replace their existing notification; legacy topics retain their grouping.
     */
    private fun notificationId(push: OmissionPush): Int =
        (push.deliveryId ?: (push.kind.name + push.olderAdultId)).hashCode()

    private fun openIntent(context: Context, push: OmissionPush): PendingIntent? {
        val launch =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launch
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(OmissionPushIntent.EXTRA_KIND, push.kind.name)
            .putExtra(OmissionPushIntent.EXTRA_OLDER_ADULT_ID, push.olderAdultId)
            .putExtra(OmissionPushIntent.EXTRA_MEDICATION, push.medicationName)
            .putExtra(OmissionPushIntent.EXTRA_INTAKE_ID, push.intakeId)
        return PendingIntent.getActivity(
            context,
            notificationId(push),
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
