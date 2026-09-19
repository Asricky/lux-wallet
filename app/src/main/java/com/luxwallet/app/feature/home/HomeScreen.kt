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
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    var showInfo by remember { mutableStateOf(false) }
    fun money(amount: Long) = if (hidden) "Rp ••••••" else AmountFormat.rupiah(amount)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_launcher_foreground), "Lux Wallet", Modifier.size(44.dp).background(LuxInk, RoundedCornerShape(14.dp)))
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
                Shortcut("Anggaran", Icons.Outlined.Savings, Modifier.weight(1f)) { onNavigate(LuxDestinations.BUDGETS_GOALS) }
                Shortcut("Kalkulator", Icons.Outlined.Calculate, Modifier.weight(1f)) { onNavigate(LuxDestinations.CALCULATOR) }
                Shortcut("Tinjau", Icons.Outlined.FactCheck, Modifier.weight(1f)) { onNavigate(LuxDestinations.NEEDS_REVIEW) }
            }
        }
        state.allowance?.let { allowance ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ruang belanja hari ini", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            IconButton({ showInfo = true }) { Icon(Icons.Outlined.Info, "Cara menghitung ruang belanja") }
                        }
                        Text(money(allowance.remainingToday.coerceAtLeast(0)), style = MaterialTheme.typography.headlineMedium)
                        Text("Sisa uang belanja bebas", style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { (allowance.usedPercent / 100).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(),
                            color = if (allowance.remainingToday < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        Text(if (hidden) "Penggunaan •••" else "${allowance.usedPercent.toInt()}% terpakai · ${money(allowance.todaySpent)} dari ${money(allowance.dailyLimit)}",
                            style = MaterialTheme.typography.bodySmall)
                        Text("Transportasi rutin & target tabungan sudah dipisahkan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (allowance.income == 0L) TextButton({ onNavigate(LuxDestinations.BUDGETS_GOALS) }) { Text("Isi pendapatan untuk menghitung anggaran") }
                    }
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
    if (showInfo) state.allowance?.let { a ->
        AlertDialog(onDismissRequest = { showInfo = false }, title = { Text("Dari mana angkanya?") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pendapatan bulanan: ${money(a.income)}")
                Text("Kewajiban & cadangan lain: −${money(a.otherCommitments)}")
                Text("Tabungan bisnis: −${money(a.businessSavings)}")
                Text("Investasi: −${money(a.investment)}")
                Text("Transportasi (${a.transportDays} hari): −${money(a.transportReserve)}")
                Text("Belanja sebelum hari ini: −${money(a.spentBeforeToday)}")
                Text("Sisanya dibagi ${a.remainingDays} hari, termasuk hari ini. Alokasi hari ini dikurangi belanja hari ini menjadi sisa ruang belanja.")
                Text("Top-up sendiri hanya pindah saldo. Tandai biaya perjalanan sebagai Transportasi rutin; tidak ditebak dari nominal Rp6.000.", style = MaterialTheme.typography.bodySmall)
            } }, confirmButton = { TextButton({ showInfo = false }) { Text("Mengerti") } },
            dismissButton = { TextButton({ showInfo = false; onNavigate(LuxDestinations.BUDGETS_GOALS) }) { Text("Ubah rencana") } })
    }
}
@Composable private fun Shortcut(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick, Modifier.size(48.dp)) { Icon(icon, label) }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
    }
}
