package com.luxwallet.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

/** Event-driven recovery with at most three backoff attempts; never a polling service. */
object ListenerRecovery {
    const val WORK = "listener_recovery"
    fun enqueue(context: Context) {
        if (!NotificationAccess.isGranted(context) || NotificationAccess.connected.value) return
        NotificationAccess.requestRebind(context)
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ListenerRecoveryWorker>().setInitialDelay(30, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build())
    }
    fun shouldRetry(granted: Boolean, connected: Boolean, attempt: Int) = granted && !connected && attempt < 2
}
class ListenerRecoveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val granted = NotificationAccess.isGranted(applicationContext)
        if (!granted || NotificationAccess.connected.value) return Result.success()
        NotificationAccess.requestRebind(applicationContext)
        return if (ListenerRecovery.shouldRetry(granted, NotificationAccess.connected.value, runAttemptCount)) Result.retry() else Result.success()
    }
}
class ListenerRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED)
            ListenerRecovery.enqueue(context)
    }
}
