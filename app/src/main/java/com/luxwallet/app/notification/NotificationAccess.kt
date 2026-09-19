package com.luxwallet.app.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService

/**
 * Notification Access is the single switch the whole capture pipeline depends on. Without it the
 * listener is never bound and nothing is recorded — so the app has to be able to *tell* the user
 * that, not fail silently.
 */
object NotificationAccess {
    val connected = kotlinx.coroutines.flow.MutableStateFlow(false)
    val lastCaptureError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    private const val ENABLED_LISTENERS = "enabled_notification_listeners"

    fun component(context: Context) =
        ComponentName(context.applicationContext, LuxNotificationListenerService::class.java)

    /** True when the user has granted Notification Access to this build. */
    fun isGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, ENABLED_LISTENERS) ?: return false
        val expected = component(context)
        return flat.split(':').any { entry ->
            ComponentName.unflattenFromString(entry)?.let {
                it.packageName == expected.packageName && it.className == expected.className
            } == true
        }
    }

    fun settingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * Asks the system to re-bind the listener. Android unbinds notification listeners after an app
     * update, a crash, or an aggressive battery kill and does not always re-bind on its own, which
     * is the classic "I granted access but nothing is captured" failure.
     */
    fun requestRebind(context: Context) {
        if (!isGranted(context)) return
        runCatching { NotificationListenerService.requestRebind(component(context)) }
    }
}
