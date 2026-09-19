package com.luxwallet.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.R
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.core.ui.theme.*
import com.luxwallet.app.navigation.LuxDestinations
import kotlinx.coroutines.launch

@Composable fun HomeScreen(onNavigate: (String) -> Unit = {}) {
    val viewModel = luxViewModel { HomeViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()
    val planner = luxViewModel { com.luxwallet.app.feature.planner.PlannerViewModel(it) }
    val planState by planner.state.collectAsState()
    val status = planState.status
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    val iconMode by app.preferences.lumiIcon.collectAsState(initial = "CALM")
    LaunchedEffect(iconMode, lumiMood(status), planState.loaded) {
        if (iconMode == "AUTO" && planState.loaded) runCatching { LumiLauncher.apply(app, lumiMood(status)) }
    }
    val scope = rememberCoroutineScope()
    var showInfo by remember { mutableStateOf(false) }
    fun money(amount: Long) = if (hidden) "Rp ••••••" else AmountFormat.rupiah(amount)
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Lumi(lumiMood(status), Modifier.size(52.dp))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("LUX WALLET", style = MaterialTheme.typography.titleMedium)
                    Text("Selamat datang kembali", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                NotificationIndicator { onNavigate(LuxDestinations.NOTIFICATION_SETTINGS) }
                IconButton({ onNavigate(LuxDestinations.SETTINGS) }) { Icon(Icons.Outlined.Settings, "Pengaturan") }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF35382F), LuxInk))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = LuxGold)
                    Text("  Kekayaan bersih", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton({ scope.launch { app.preferences.setAmountsHidden(!hidden) } }) {
                        Icon(if (hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (hidden) "Tampilkan nominal" else "Sembunyikan nominal", tint = LuxGold)
                    }
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (state.isLoading) "Memuat…" else money(state.netWorth), style = MaterialTheme.typography.headlineLarge)
                    Text("Total aset − kewajiban", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(Modifier.padding(top = 8.dp))
                    TextButton({ onNavigate(LuxDestinations.ASSETS) }, contentPadding = PaddingValues(0.dp)) { Text("Lihat & atur aset  →") }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Shortcut("Riwayat", Icons.Outlined.ReceiptLong, Modifier.weight(1f)) { onNavigate(LuxDestinations.TRANSACTIONS) }
                Shortcut("Rencana", Icons.Outlined.Savings, Modifier.weight(1f)) { onNavigate(LuxDestinations.PLANNER) }
                Shortcut("Kalkulator", Icons.Outlined.Calculate, Modifier.weight(1f)) { onNavigate(LuxDestinations.CALCULATOR) }
                Shortcut("Tinjau", Icons.Outlined.FactCheck, Modifier.weight(1f)) { onNavigate(LuxDestinations.NEEDS_REVIEW) }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ruang belanja hari ini", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        IconButton({ showInfo = true }) { Icon(Icons.Outlined.Info, "Cara menghitung ruang belanja") }
                    }
                    if (status == null || status.expired) {
                        Text(if (status == null) "Konfirmasi saldo yang tersedia" else "Waktunya memperbarui rencana", style = MaterialTheme.typography.titleLarge)
                        Text("Atur kebutuhan sampai pemasukan tanggal 25 atau 1. Uang yang belum diterima belum bisa dibelanjakan.")
                    } else {
                        Text(money(status.remainingToday.coerceAtLeast(0)), style = MaterialTheme.typography.headlineMedium)
                        Text("Sampai ${status.plan.end.format(com.luxwallet.app.feature.planner.planDateFormat)}", style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { (status.usedPercent / 100).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(),
                            color = if (status.remainingToday < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        Text(if (hidden) "Penggunaan disembunyikan" else "${status.usedPercent.toInt()}% terpakai · ${money(status.spentToday)} dari ${money(status.dailyBudget)}", style = MaterialTheme.typography.bodySmall)
                        if (status.freeRemaining < 0) Text("Alokasi melebihi dana tersedia. Tinjau rencana sebelum belanja.", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton({ onNavigate(LuxDestinations.PLANNER) }) { Text("Atur rencana sampai gajian") }
                }
            }
        }
        item {
            val advice = com.luxwallet.app.engine.MoneyCoach.advise(status, planState.today)
            OutlinedCard(onClick = { onNavigate(LuxDestinations.COACH) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Lumi(lumiMood(status), Modifier.size(60.dp))
                    Column(Modifier.weight(1f)) { Text("Saran Lumi", style = MaterialTheme.typography.labelMedium); Text(advice.title, style = MaterialTheme.typography.titleMedium) }
                    Icon(Icons.Outlined.ChevronRight, "Buka saran Lumi")
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Riwayat transaksi", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton({ onNavigate(LuxDestinations.TRANSACTIONS) }) { Text("Semua") }
            }
        }
        if (state.recentTransactions.isEmpty()) item {
            OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
                Text("Belum ada transaksi")
                TextButton({ onNavigate(LuxDestinations.QUICK_ADD) }) { Text("Catat transaksi pertama") }
            } }
        }
        items(state.recentTransactions, key = { it.id }) { tx ->
            TransactionCard(tx, state.accountNames[tx.sourceAccountId], state.accountNames[tx.destinationAccountId],
                state.categoryNames[tx.categoryId], { onNavigate(LuxDestinations.transactionDetail(tx.id)) }, hideAmounts = hidden)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sebaran aset", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                IconButton({ scope.launch { app.preferences.setAmountsHidden(!hidden) } }) { Icon(if (hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Tampilkan atau sembunyikan aset") }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!hidden && state.assetDistribution.isNotEmpty()) DonutChart(state.assetDistribution, money(state.assetDistribution.sumOf { it.value }.toLong()), "Aset positif")
                    if (hidden) Text("Nominal & proporsi aset disembunyikan", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium)
                    state.assetDistribution.forEach { slice ->
                        Row(Modifier.fillMaxWidth()) { Text(slice.label, Modifier.weight(1f)); Text(money(slice.value.toLong())) }
                    }
                    if (state.assetDistribution.isEmpty()) TextButton({ onNavigate(LuxDestinations.ASSETS) }) { Text("Tambahkan asetmu") }
                }
            }
        }
    }
    if (showInfo) AlertDialog(onDismissRequest = { showInfo = false }, title = { Text("Dari mana angkanya?") },
        text = { Text("Saldo likuid yang kamu konfirmasi dikurangi tagihan, dana penyangga, modal bisnis, investasi, dan transportasi sampai gajian. Sisanya dibagi jumlah hari rencana. Belanja mengurangi sisa harian dan dibatasi dana bebas yang masih ada.\n\nTop-up sendiri tidak mengurangi budget. Tagihan terencana dan Transportasi rutin memakai cadangannya dahulu. Perkiraan pemasukan tidak dihitung sebagai saldo. Lihat rincian pada rencana sampai gajian.") },
        confirmButton = { TextButton({ showInfo = false; onNavigate(LuxDestinations.PLANNER) }) { Text("Lihat rencana") } },
        dismissButton = { TextButton({ showInfo = false }) { Text("Tutup") } })
}
@Composable private fun Shortcut(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick, Modifier.size(48.dp)) { Icon(icon, label) }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
    }
}
