package com.luxwallet.app.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.MainActivity
import com.luxwallet.app.R
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.engine.*
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId

object BudgetAlerts {
    suspend fun check(app: LuxWalletApp, now: Long) {
        val date = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val plan = app.paydayPlanRepository.plans.first().maxByOrNull { it.capturedAt } ?: return
        val categories = app.categoryRepository.observeAll().first()
        val status = PaydayMath.status(plan, app.transactionRepository.observeAll().first(),
            categories.filter { it.name == TransportPlan.CATEGORY }.map { it.id }.toSet(),
            categories.filter { it.name == PaydayPlan.BILLS_CATEGORY }.map { it.id }.toSet(), date)
        if (status.expired) return
        val level = when { status.dailyBudget <= 0 && status.spentToday > 0 -> 120; status.usedPercent > 120 -> 120; status.usedPercent > 100 -> 100; status.usedPercent >= 75 -> 75; else -> return }
        val previous = app.preferences.budgetAlertMarker.first().split('|')
        if (previous.firstOrNull() == date.toString() && (previous.lastOrNull()?.toIntOrNull() ?: 0) >= level) return
        if (!TransactionNotifications.allowed(app)) return
        val title = when(level) { 120 -> "Yuk, susun ulang belanja hari ini"; 100 -> "Budget hari ini sudah terlewati"; else -> "Budget hari ini mulai menipis" }
        val intent = Intent(app, MainActivity::class.java).putExtra("open_coach", true)
        val pending = PendingIntent.getActivity(app, 3102, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val public = NotificationCompat.Builder(app, TransactionNotifications.CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle("Lumi").setContentText("Periksa rencana harianmu.").build()
        val notification = NotificationCompat.Builder(app, TransactionNotifications.CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle(title).setContentText("Dahulukan kebutuhan utama. Buka rencana untuk melihat pilihan alokasi.")
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(public).build()
        try {
            NotificationManagerCompat.from(app).notify("budget:$date", 2, notification)
            app.preferences.setBudgetAlertMarker("$date|$level")
        } catch (_: SecurityException) { /* permission can be revoked between the check and notify */ }
    }
}
