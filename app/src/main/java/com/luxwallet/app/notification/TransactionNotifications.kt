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
import androidx.room.withTransaction
import androidx.work.*
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.MainActivity
import com.luxwallet.app.R
import com.luxwallet.app.core.common.*
import com.luxwallet.app.core.database.entity.TransactionEntity
import com.luxwallet.app.core.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

object TransactionFeedback {
    data class Event(val id: Long, val title: String)
    val events = MutableSharedFlow<Event>(extraBufferCapacity = 8)
}
object TransactionNotifications {
    const val CHANNEL = "lux_wallet_transactions"
    const val WORK = "transaction_confirmations"
    fun createChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Lumi Transactions", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Konfirmasi pencatatan transaksi dan peringatan budget"
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            })
    }
    fun allowed(context: Context): Boolean = (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
    fun schedule(context: Context, dueAt: Long) {
        WorkManager.getInstance(context).enqueueUniqueWork("${WORK}_$dueAt", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<TransactionConfirmationWorker>().setInitialDelay((dueAt - System.currentTimeMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS).build())
    }
    fun option(tx: TransactionEntity) = when {
        tx.reviewStatus == ReviewStatus.NEEDS_REVIEW -> NotificationOptions.REVIEW
        tx.isInternalTransfer || tx.type in setOf(TransactionType.INTERNAL_TRANSFER, TransactionType.EXTERNAL_TRANSFER, TransactionType.EWALLET_TOPUP) -> NotificationOptions.TRANSFER
        tx.direction == TransactionDirection.IN -> NotificationOptions.INCOME
        else -> NotificationOptions.EXPENSE
    }
    fun title(tx: TransactionEntity) = when(option(tx)) {
        NotificationOptions.REVIEW -> "Transaksi perlu dicek"
        NotificationOptions.TRANSFER -> "Transfer tercatat"
        NotificationOptions.INCOME -> "Pemasukan tercatat"
        else -> if (tx.type == TransactionType.QRIS_PAYMENT) "Pembayaran tercatat" else "Pengeluaran tercatat"
    }
    fun enabled(tx: TransactionEntity, options: Map<NotificationOptions, Boolean>) =
        tx.reviewStatus != ReviewStatus.IGNORED && tx.type != TransactionType.BALANCE_ADJUSTMENT &&
            options[NotificationOptions.RECORDED] != false && options[option(tx)] != false

    fun send(context: Context, tx: TransactionEntity, source: String?, destination: String?, hidden: Boolean): Boolean {
        if (!allowed(context)) return false
        val text = when {
            hidden -> "Buka Lumi untuk melihat rincian."
            tx.reviewStatus == ReviewStatus.NEEDS_REVIEW -> "${AmountFormat.rupiah(tx.amount)} terdeteksi. Periksa rekening atau kategorinya."
            tx.isInternalTransfer -> "${AmountFormat.rupiah(tx.amount)} · ${source ?: "Rekening"} ke ${destination ?: "Rekening"}"
            else -> "${AmountFormat.rupiah(tx.amount)} · ${source ?: "Rekening"}${(tx.merchantName ?: tx.counterpartyName)?.let { " · $it" } ?: ""}"
        }
        val intent = Intent(context, MainActivity::class.java).putExtra("transaction_id", tx.id)
            .setData(android.net.Uri.parse("luxwallet://transaction/${tx.id}"))
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val public = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle("Lumi").setContentText("Ada pembaruan transaksi.").build()
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification_lumi)
            .setContentTitle(title(tx)).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(public).build()
        return try { NotificationManagerCompat.from(context).notify("transaction:${tx.id}", 1, notification); true }
        catch (_: SecurityException) { false }
    }
}

class TransactionConfirmationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as LuxWalletApp
        return try {
            dispatch(app)
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.retry()
        }
    }
    companion object {
        private val dispatchMutex = Mutex()
        suspend fun dispatch(app: LuxWalletApp, now: Long = System.currentTimeMillis()) = dispatchMutex.withLock {
            val options = app.preferences.notificationOptions.first()
            val hidden = app.preferences.amountsHidden.first()
            val dao = app.database.transactionConfirmationDao()
            val events = mutableListOf<TransactionFeedback.Event>()
            app.database.withTransaction {
                val accounts = app.database.accountDao().getAllAccountsOnce().associateBy { it.id }
                for (receipt in dao.due(now)) {
                    val tx = app.database.transactionDao().getById(receipt.transactionId)
                    if (tx != null && TransactionNotifications.enabled(tx, options)) {
                        TransactionNotifications.send(app, tx, accounts[tx.sourceAccountId]?.name, accounts[tx.destinationAccountId]?.name, hidden)
                        events.add(TransactionFeedback.Event(tx.id, TransactionNotifications.title(tx)))
                    }
                    // Consume disabled/denied alerts too: enabling later must not replay old financial history.
                    dao.handled(receipt.transactionId)
                }
            }
            events.forEach { TransactionFeedback.events.tryEmit(it) }
            if (options[NotificationOptions.BUDGET] != false) BudgetAlerts.check(app, now)
        }
    }
}
