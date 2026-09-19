package com.luxwallet.app.notification

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.MainActivity
import com.luxwallet.app.R
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.engine.*
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object CoachNotifications {
    const val CHANNEL = "money_coach"
    const val WORK = "money_coach_daily"
    const val ID = 3001
    fun allowed(context: Context): Boolean = (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
    fun configure(context: Context, enabled: Boolean) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Saran keuangan Lumi", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Pengingat rencana uang dan edukasi investasi, maksimal sekali sehari"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        })
        if (enabled) WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CoachWorker>(1, TimeUnit.HOURS).build())
        else { WorkManager.getInstance(context).cancelUniqueWork(WORK); manager.cancel(ID) }
    }
    fun send(context: Context, advice: CoachAdvice): Boolean {
        if (!allowed(context)) return false
        val intent = Intent(context, MainActivity::class.java).putExtra("open_coach", true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val public = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle("Lumi · Lux Wallet").setContentText("Ada saran untuk rencana uangmu.").build()
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle("Lumi · ${advice.title}").setContentText(advice.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(advice.message))
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(public).build()
        return try { NotificationManagerCompat.from(context).notify(ID, notification); true } catch (_: SecurityException) { false }
    }
}

class CoachWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as LuxWalletApp
        if (!app.preferences.coachEnabled.first() || !app.preferences.onboardingComplete.first()) return Result.success()
        val now = LocalDateTime.now()
        if (!MoneyCoach.canNotify(now.toLocalDate(), app.preferences.coachLastDate.first(), now.hour)) return Result.success()
        return try {
            val plans = app.paydayPlanRepository.plans.first()
            val categories = app.categoryRepository.observeAll().first()
            val transportIds = categories.filter { it.name == TransportPlan.CATEGORY }.map { it.id }.toSet()
            val billIds = categories.filter { it.name == PaydayPlan.BILLS_CATEGORY }.map { it.id }.toSet()
            val status = plans.maxByOrNull { it.capturedAt }?.let {
                PaydayMath.status(it, app.transactionRepository.observeAll().first(), transportIds, billIds, now.toLocalDate())
            }
            if (CoachNotifications.send(app, MoneyCoach.advise(status, now.toLocalDate()))) app.preferences.setCoachLastDate(now.toLocalDate().toString())
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.retry()
        }
    }
}
