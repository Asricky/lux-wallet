package com.luxwallet.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.luxwallet.app.core.model.ThemeMode
import com.luxwallet.app.core.security.BiometricAuthManager
import com.luxwallet.app.core.ui.theme.LuxThemePreference
import com.luxwallet.app.core.ui.theme.LuxWalletTheme
import com.luxwallet.app.feature.onboarding.OnboardingScreen
import com.luxwallet.app.navigation.LuxDestinations
import com.luxwallet.app.navigation.LuxNavGraph
import kotlinx.coroutines.flow.collectLatest

class MainActivity : FragmentActivity() {
    var openCoach by mutableStateOf(false)
    var openTransaction by mutableStateOf<Long?>(null)
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openCoach = intent.getBooleanExtra("open_coach", false)
        openTransaction = intent.getLongExtra("transaction_id", 0).takeIf { it > 0 }
    }
    override fun onResume() {
        super.onResume()
        com.luxwallet.app.notification.NotificationAccess.requestRebind(this)
        androidx.work.WorkManager.getInstance(this).enqueueUniqueWork(
            com.luxwallet.app.notification.NotificationProcessingWorker.UNIQUE_WORK_NAME,
            androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE,
            androidx.work.OneTimeWorkRequestBuilder<com.luxwallet.app.notification.NotificationProcessingWorker>().build()
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openCoach = intent.getBooleanExtra("open_coach", false)
        openTransaction = intent.getLongExtra("transaction_id", 0).takeIf { it > 0 }
        enableEdgeToEdge()

        val app = application as LuxWalletApp

        setContent {
            val themeMode by app.preferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val screenshotProtection by app.preferences.screenshotProtectionEnabled.collectAsState(initial = false)

            LaunchedEffect(screenshotProtection) {
                if (screenshotProtection) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            LuxWalletTheme(
                themePreference = when (themeMode) {
                    ThemeMode.SYSTEM -> LuxThemePreference.SYSTEM
                    ThemeMode.LIGHT -> LuxThemePreference.LIGHT
                    ThemeMode.DARK -> LuxThemePreference.DARK
                }
            ) {
                val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
                androidx.compose.runtime.CompositionLocalProvider(com.luxwallet.app.core.common.LocalAmountsHidden provides hidden) {
                    LuxWalletRoot(this)
                }
            }
        }
    }
}

@Composable
private fun LuxWalletRoot(activity: FragmentActivity) {
    val app = activity.application as LuxWalletApp
    val onboardingComplete by app.preferences.onboardingComplete.collectAsState(initial = null)
    val navController = rememberNavController()
    val biometricLockEnabled by app.preferences.biometricLockEnabled.collectAsState(initial = null)

    when (onboardingComplete) {
        null -> Unit // still loading preferences; render nothing to avoid a flash of onboarding
        false -> OnboardingScreen()
        true -> {
            if (biometricLockEnabled == null) return
            var unlocked by remember(biometricLockEnabled) { mutableStateOf(biometricLockEnabled != true) }
            var retryToken by remember { mutableStateOf(0) }
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner, biometricLockEnabled) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP && biometricLockEnabled == true) unlocked = false
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_START) retryToken++
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(biometricLockEnabled, retryToken) {
                if (biometricLockEnabled == true && !unlocked) {
                    BiometricAuthManager.authenticate(activity).collectLatest { success ->
                        unlocked = success
                    }
                }
            }

            if (unlocked) {
                com.luxwallet.app.navigation.LuxAppScaffold(navController)
                LaunchedEffect((activity as? MainActivity)?.openTransaction) {
                    (activity as? MainActivity)?.openTransaction?.let { id ->
                        navController.navigate(LuxDestinations.transactionDetail(id)) { launchSingleTop = true }
                        activity.openTransaction = null
                        activity.intent.removeExtra("transaction_id")
                    }
                }
                LaunchedEffect((activity as? MainActivity)?.openCoach) {
                    if ((activity as? MainActivity)?.openCoach == true) {
                        navController.navigate(LuxDestinations.COACH) { launchSingleTop = true }
                        activity.openCoach = false
                        activity.intent.removeExtra("open_coach")
                    }
                }
            } else {
                LockedScreen(onRetry = { retryToken++ })
            }
        }
    }
}

@Composable
private fun LockedScreen(onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        Modifier.padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Lumi terkunci")
        androidx.compose.material3.TextButton(onClick = onRetry) { Text("Buka kunci") }
    }
}
