package com.luxwallet.app.integration

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.NotificationOptions
import com.luxwallet.app.core.model.*
import com.luxwallet.app.notification.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = LuxWalletApp::class)
class TransactionConfirmationTest {
    private val app get() = ApplicationProvider.getApplicationContext<LuxWalletApp>()
    private var source = 0L
    @Before fun prepare() = runBlocking {
        app.preferences.planV2Ready.first { it }
        app.applicationScope.coroutineContext[Job]?.cancelAndJoin()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        app.preferences.setAmountsHidden(false)
        source = app.accountRepository.createAccount("BCA", AccountKind.BANK, AccountProvider.BCA, 100_000, 0)
        Unit
    }
    @After fun finish() { app.database.close() }
    @Test fun confirmationIsDurableOnceOnlyAndLinksToCommittedTransaction() = runBlocking {
        val id = app.transactionRepository.insertManual(TransactionType.QRIS_PAYMENT, TransactionDirection.OUT, 3, source, merchantName = "Toko uji")
        assertEquals(99_997, app.accountRepository.getById(source)!!.currentEstimatedBalance.toInt())
        assertFalse(app.database.transactionConfirmationDao().get(id)!!.handled)
        TransactionConfirmationWorker.dispatch(app)
        val manager = app.getSystemService(NotificationManager::class.java)
        val first = shadowOf(manager).getNotification("transaction:$id", 1)
        assertNotNull(first)
        assertEquals("Pembayaran tercatat", first.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertTrue(first.extras.getCharSequence(Notification.EXTRA_TEXT).toString().contains("Rp3"))
        assertFalse(first.publicVersion.extras.toString().contains("Toko uji"))
        assertEquals(id, shadowOf(first.contentIntent).savedIntent.getLongExtra("transaction_id", 0))
        manager.cancelAll()
        TransactionConfirmationWorker.dispatch(app)
        assertTrue(manager.activeNotifications.isEmpty())
        assertTrue(app.database.transactionConfirmationDao().get(id)!!.handled)
    }
    @Test fun filtersAndPermissionDoNotReplayOldTransactions() = runBlocking {
        app.preferences.setNotificationOption(NotificationOptions.EXPENSE, false)
        val id = app.transactionRepository.insertManual(TransactionType.EXPENSE, TransactionDirection.OUT, 50, source)
        TransactionConfirmationWorker.dispatch(app)
        val manager = app.getSystemService(NotificationManager::class.java)
        assertTrue(manager.activeNotifications.isEmpty())
        app.preferences.setNotificationOption(NotificationOptions.EXPENSE, true)
        TransactionConfirmationWorker.dispatch(app)
        assertTrue(manager.activeNotifications.isEmpty())
        assertTrue(app.database.transactionConfirmationDao().get(id)!!.handled)
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        app.transactionRepository.insertManual(TransactionType.INCOME, TransactionDirection.IN, 100, source)
        TransactionConfirmationWorker.dispatch(app)
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        TransactionConfirmationWorker.dispatch(app)
        assertTrue(manager.activeNotifications.isEmpty())
    }
    @Test fun hiddenAmountsAndTransferUseOnePrivateConfirmation() = runBlocking {
        app.preferences.setAmountsHidden(true)
        val destination = app.accountRepository.createAccount("GoPay", AccountKind.EWALLET, AccountProvider.GOPAY, 0, 0)
        val id = app.transactionRepository.insertManual(TransactionType.INTERNAL_TRANSFER, TransactionDirection.OUT, 10000, source, destination)
        TransactionConfirmationWorker.dispatch(app)
        val manager = app.getSystemService(NotificationManager::class.java)
        assertEquals(1, manager.activeNotifications.size)
        val posted = shadowOf(manager).getNotification("transaction:$id", 1)
        assertEquals("Transfer tercatat", posted.extras.getCharSequence(Notification.EXTRA_TITLE))
        assertFalse(posted.extras.toString().contains("10.000"))
        assertEquals(100_000, app.database.accountDao().getAllAccountsOnce().sumOf { it.currentEstimatedBalance }.toInt())
    }
    @Test fun ownNotificationsAreRejectedEvenWithCustomBankMapping() = runBlocking {
        app.preferences.mapPackage(app.packageName, SourceApp.MYBCA)
        val controller = Robolectric.buildService(LuxNotificationListenerService::class.java).create()
        try {
            val notification = Notification.Builder(app, TransactionNotifications.CHANNEL).setContentTitle("Catatan Finansial")
                .setContentText("Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan.").build()
            val sbn = StatusBarNotification(app.packageName, app.packageName, 7, null, 0, 0, 0, notification, android.os.Process.myUserHandle(), System.currentTimeMillis())
            controller.get().onNotificationPosted(sbn)
            assertTrue(app.notificationRepository.getPending().isEmpty())
            assertTrue(app.database.transactionDao().getAllOnce().isEmpty())
        } finally { controller.destroy() }
    }
}
