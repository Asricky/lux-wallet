@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.luxwallet.app.navigation

import androidx.lifecycle.repeatOnLifecycle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

val mainRoutes = listOf(LuxDestinations.HOME, LuxDestinations.CALENDAR, LuxDestinations.ASSETS, LuxDestinations.MORE)

fun NavHostController.openScreen(route: String) {
    if (route in mainRoutes) {
        if (currentDestination?.route == route) return
        // Discard detail/form destinations before saving only the current main tab.
        while (currentDestination?.route !in mainRoutes && previousBackStackEntry != null) popBackStack()
        navigate(route) {
            popUpTo(LuxDestinations.HOME) { saveState = true }
            restoreState = true
            launchSingleTop = true
        }
    } else navigate(route) { launchSingleTop = true }
}

private fun screenTitle(route: String?) = when (route) {
    LuxDestinations.ALERT_SETTINGS -> "Notifikasi Lumi"
    LuxDestinations.QUICK_ADD -> "Catat transaksi"
    LuxDestinations.TRANSACTION_DETAIL -> "Detail transaksi"
    LuxDestinations.TRANSACTIONS -> "Riwayat transaksi"
    LuxDestinations.CASHFLOW -> "Arus kas"
    LuxDestinations.PLANNER -> "Rencana sampai gajian"
    LuxDestinations.BUDGETS_GOALS -> "Anggaran & target"
    LuxDestinations.CALCULATOR -> "Kalkulator"
    LuxDestinations.SETTINGS -> "Pengaturan"
    LuxDestinations.NOTIFICATION_SETTINGS -> "Pemantauan notifikasi"
    LuxDestinations.COACH -> "Saran Lumi"
    LuxDestinations.ACCOUNTS -> "Rekening & dompet"
    LuxDestinations.CATEGORIES -> "Kategori"
    LuxDestinations.RULES -> "Aturan merchant"
    LuxDestinations.NEEDS_REVIEW -> "Perlu ditinjau"
    LuxDestinations.INSIGHTS -> "Wawasan keuangan"
    LuxDestinations.NOTIFICATION_LAB -> "Diagnostik notifikasi"
    else -> "Lumi"
}

@Composable
fun LuxAppScaffold(navController: NavHostController = rememberNavController(), content: @Composable (NavHostController) -> Unit = { LuxNavGraph(it) }) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: LuxDestinations.HOME
    val isMain = route in mainRoutes
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val snackbar = remember { SnackbarHostState() }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            com.luxwallet.app.notification.TransactionFeedback.events.collect { event ->
                kotlinx.coroutines.withTimeoutOrNull(2800) {
                    if (snackbar.showSnackbar(event.title, "Lihat", duration = SnackbarDuration.Indefinite) == SnackbarResult.ActionPerformed) navController.openScreen(LuxDestinations.transactionDetail(event.id))
                }
            }
        }
    }
    val focus = LocalFocusManager.current
    val labels = listOf("Beranda", "Kalender", "Aset", "Lainnya")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.CalendarMonth, Icons.Outlined.AccountBalanceWallet, Icons.Outlined.GridView)
    BackHandler(isMain && route != LuxDestinations.HOME) { navController.openScreen(LuxDestinations.HOME) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp
        Row(Modifier.fillMaxSize()) {
            if (wide && isMain) NavigationRail {
                mainRoutes.forEachIndexed { i, target ->
                    NavigationRailItem(selected = route == target, onClick = { focus.clearFocus(); navController.openScreen(target) },
                        icon = { Icon(icons[i], null) }, label = { Text(labels[i]) })
                }
            }
            Scaffold(modifier = Modifier.weight(1f), snackbarHost = { SnackbarHost(snackbar) }, topBar = {
                if (!isMain) TopAppBar(title = { Text(screenTitle(route), maxLines = 1) }, navigationIcon = {
                    IconButton(onClick = { focus.clearFocus(); dispatcher?.onBackPressed() ?: navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali")
                    }
                })
            }, bottomBar = {
                if (isMain && !wide) NavigationBar(tonalElevation = 0.dp, containerColor = MaterialTheme.colorScheme.surface) {
                    mainRoutes.forEachIndexed { i, target ->
                        NavigationBarItem(selected = route == target, onClick = { focus.clearFocus(); navController.openScreen(target) },
                            icon = { Icon(icons[i], null) }, label = { Text(labels[i], maxLines = 1) })
                    }
                }
            }, floatingActionButton = {
                if (isMain && route != LuxDestinations.MORE) FloatingActionButton(onClick = { navController.openScreen(LuxDestinations.QUICK_ADD) }) {
                    Icon(Icons.Outlined.Add, "Catat transaksi")
                }
            }) { padding ->
                Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content(navController) }
            }
        }
    }
}
