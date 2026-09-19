package com.luxwallet.app.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.database.entity.NotificationObservationEntity
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.data.NotificationRepository
import com.luxwallet.app.parser.core.ParserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The single entry point for all transaction data (PRD §7 first step). Everything not in
 * [SupportedPackages] — Gmail included — is dropped in [onNotificationPosted] before any parsing
 * or storage happens; there is no path from an unsupported package into the database.
 */
class LuxNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val captureLock = Mutex()

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationAccess.connected.value = true
        enqueueProcessing()
        runCatching { activeNotifications?.forEach(::onNotificationPosted) }
    }

    override fun onListenerDisconnected() {
        NotificationAccess.connected.value = false
        NotificationAccess.requestRebind(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
          captureLock.withLock {
           try {
            val app = applicationContext as LuxWalletApp
            val sourceApp = SupportedPackages.resolve(sbn.packageName, app.preferences.packageMappings.first())
            if (sourceApp == null) {
                if (app.preferences.packageDiscoveryEnabled.first()) app.preferences.recordDiscoveredPackage(sbn.packageName)
                return@withLock
            }
            val enabledSources = app.preferences.enabledSources.first()
            if (sourceApp !in enabledSources) return@withLock
            if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return@withLock

            val extras = sbn.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.map { it.toString() }
                .orEmpty()

            if (title.isBlank() && text.isBlank() && bigText.isNullOrBlank() && textLines.isEmpty()) return@withLock

            val postedAt = sbn.postTime
            val hash = NotificationRepository.hashPayload(sourceApp.name, sbn.packageName, title, text, postedAt,
                sbn.key, bigText, subText, textLines)

            val observation = NotificationObservationEntity(
                sourceApp = sourceApp,
                packageName = sbn.packageName,
                notificationKey = sbn.key,
                title = title,
                text = text,
                bigText = bigText,
                subText = subText,
                textLines = textLines.joinToString("\n").ifBlank { null },
                postedAt = postedAt,
                receivedAt = System.currentTimeMillis(),
                rawPayloadHash = hash,
                parserVersion = ParserRegistry.PARSER_VERSION,
                parseStatus = ParseStatus.PENDING
            )

            val insertedId = app.notificationRepository.insertIfNew(observation)
            if (insertedId != null) {
                NotificationAccess.lastCaptureError.value = null
                enqueueProcessing()
            }
           } catch (cancelled: CancellationException) {
               throw cancelled
           } catch (error: Exception) {
               NotificationAccess.lastCaptureError.value = "Notifikasi belum tersimpan. Periksa ruang penyimpanan lalu sambungkan ulang."
               android.util.Log.e("LuxCapture", "Notification capture failed: ${error.javaClass.simpleName}")
           }
          }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationAccess.connected.value = false
        scope.cancel()
    }

    private fun enqueueProcessing() {
        val request = OneTimeWorkRequestBuilder<NotificationProcessingWorker>().build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(NotificationProcessingWorker.UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}
