package com.luxwallet.app.integration

import android.app.Notification
import android.content.Intent
import android.provider.Settings
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AppPreferences
import com.luxwallet.app.core.model.*
import com.luxwallet.app.notification.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationListenerService

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = LuxWalletApp::class)
class ListenerRecoveryTest {
    private val app get() = ApplicationProvider.getApplicationContext<LuxWalletApp>()
    @Before fun prepare() = runBlocking {
        app.preferences.planV2Ready.first { it }
        NotificationAccess.connected.value = false
        // WorkManager's singleton otherwise outlives Robolectric's SQLite sandbox between tests.
        org.robolectric.util.ReflectionHelpers.setStaticField(androidx.work.impl.WorkManagerImpl::class.java, "sDefaultInstance", null)
        org.robolectric.util.ReflectionHelpers.setStaticField(androidx.work.impl.WorkManagerImpl::class.java, "sDelegatedInstance", null)
        WorkManager.getInstance(app)
        Unit
    }
    @After fun finish() = runBlocking {
        Settings.Secure.putString(app.contentResolver, "enabled_notification_listeners", "")
        WorkManager.getInstance(app).cancelAllWork().result.get()
        app.applicationScope.coroutineContext[Job]?.cancelAndJoin()
        app.database.close()
        org.robolectric.util.ReflectionHelpers.setStaticField(androidx.work.impl.WorkManagerImpl::class.java, "sDefaultInstance", null)
        org.robolectric.util.ReflectionHelpers.setStaticField(androidx.work.impl.WorkManagerImpl::class.java, "sDelegatedInstance", null)
    }
    private fun grant() { Settings.Secure.putString(app.contentResolver, "enabled_notification_listeners", NotificationAccess.component(app).flattenToString()) }
    @Test fun restartWithPermissionRequestsRebindAndKeepsOneRecoveryJob() {
        grant()
        val before = ShadowNotificationListenerService.getRebindRequestCount()
        ListenerRecovery.enqueue(app)
        val first = WorkManager.getInstance(app).getWorkInfosForUniqueWork(ListenerRecovery.WORK).get().single().id
        NotificationAccess.connected.value = false // fresh process has no in-memory connection evidence
        ListenerRecovery.enqueue(app)
        assertTrue(ShadowNotificationListenerService.getRebindRequestCount() > before)
        assertEquals(first, WorkManager.getInstance(app).getWorkInfosForUniqueWork(ListenerRecovery.WORK).get().single().id)
    }
    @Test fun disconnectAndBootRequestRecoveryWithoutAnActivity() {
        grant()
        val controller = Robolectric.buildService(LuxNotificationListenerService::class.java).create()
        val before = ShadowNotificationListenerService.getRebindRequestCount()
        NotificationAccess.connected.value = true
        controller.get().onListenerDisconnected()
        assertFalse(NotificationAccess.connected.value)
        assertTrue(ShadowNotificationListenerService.getRebindRequestCount() > before)
        val disconnected = ShadowNotificationListenerService.getRebindRequestCount()
        ListenerRestartReceiver().onReceive(app, Intent(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(ShadowNotificationListenerService.getRebindRequestCount() > disconnected)
        controller.destroy()
    }
    @Test fun noPermissionMeansNoRecoveryAndRetriesAreBounded() {
        Settings.Secure.putString(app.contentResolver, "enabled_notification_listeners", "")
        val before = ShadowNotificationListenerService.getRebindRequestCount()
        ListenerRecovery.enqueue(app)
        assertEquals(before, ShadowNotificationListenerService.getRebindRequestCount())
        assertTrue(WorkManager.getInstance(app).getWorkInfosForUniqueWork(ListenerRecovery.WORK).get().isEmpty())
        assertTrue(ListenerRecovery.shouldRetry(true, false, 0))
        assertTrue(ListenerRecovery.shouldRetry(true, false, 1))
        assertFalse(ListenerRecovery.shouldRetry(true, false, 2))
        assertFalse(ListenerRecovery.shouldRetry(false, false, 0))
        assertFalse(ListenerRecovery.shouldRetry(true, true, 0))
    }
    @Test fun acceptedNotificationSurvivesServiceTeardownAndDiagnosticsPersist() = runBlocking {
        app.accountRepository.createAccount("BCA", AccountKind.BANK, AccountProvider.BCA, 100000, 0)
        val controller = Robolectric.buildService(LuxNotificationListenerService::class.java).create()
        val notification = Notification.Builder(app, TransactionNotifications.CHANNEL).setContentTitle("Catatan Finansial")
            .setContentText("Pengeluaran sebesar IDR 3.00 di kategori Belanja Bulanan.").build()
        val sbn = StatusBarNotification("com.bca.mybca", "com.bca.mybca", 7, null, 0, 0, 0, notification,
            android.os.Process.myUserHandle(), System.currentTimeMillis())
        controller.get().onNotificationPosted(sbn)
        controller.destroy()
        val observations = withTimeout(5000) { app.notificationRepository.observeAll().first { it.isNotEmpty() } }
        assertEquals(1, observations.size)
        val recorded = withTimeout(5000) { app.preferences.listenerDiagnostics.first { it.first > 0 } }
        assertEquals(recorded, AppPreferences(app).listenerDiagnostics.first())
    }
}
