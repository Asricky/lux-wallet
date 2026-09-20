package com.luxwallet.app.integration

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.ui.component.LumiLauncher
import com.luxwallet.app.core.ui.component.LumiMood
import com.luxwallet.app.engine.MoneyCoach
import com.luxwallet.app.notification.CoachNotifications
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CoachNotificationTest {
    @Test fun notificationRequiresPermissionAndHidesFinancialDetailOnLockScreen() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        try { androidx.work.WorkManager.getInstance(context) }
        catch (_: IllegalStateException) { androidx.work.WorkManager.initialize(context, androidx.work.Configuration.Builder().build()) }
        CoachNotifications.configure(context, false)
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(CoachNotifications.send(context, MoneyCoach.advise(null, LocalDate.now())))
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(CoachNotifications.send(context, MoneyCoach.advise(null, LocalDate.now())))
        val manager = context.getSystemService(NotificationManager::class.java)
        val posted = shadowOf(manager).getNotification(CoachNotifications.ID)
        assertEquals(Notification.VISIBILITY_PRIVATE, posted.visibility)
        assertNotNull(posted.contentIntent)
        assertEquals("Ada saran untuk rencana uangmu.", posted.publicVersion.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
        CoachNotifications.configure(context, false)
        assertNull(shadowOf(manager).getNotification(CoachNotifications.ID))
    }
    @Test fun changingMascotAlwaysLeavesExactlyOneLauncherEnabled() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val names = listOf("LumiCalm", "LumiHappy", "LumiFocus")
        assertNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
        LumiMood.entries.forEach { mood ->
            LumiLauncher.apply(context, mood)
            val enabled = names.filter { name -> context.packageManager.getComponentEnabledSetting(
                ComponentName(context.packageName, "com.luxwallet.app.$name")) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED }
            assertEquals(listOf(names[when(mood) { LumiMood.HAPPY, LumiMood.PROUD, LumiMood.EXCITED -> 1; LumiMood.CALM, LumiMood.CURIOUS -> 0; else -> 2 }]), enabled)
            val launcherIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            assertNotNull(launcherIntent)
            assertEquals("com.luxwallet.app.${names[when(mood) { LumiMood.HAPPY, LumiMood.PROUD, LumiMood.EXCITED -> 1; LumiMood.CALM, LumiMood.CURIOUS -> 0; else -> 2 }]}", launcherIntent!!.component!!.className)
        }
    }
}
