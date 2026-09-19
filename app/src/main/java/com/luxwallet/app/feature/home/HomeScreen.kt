package com.luxwallet.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.luxwallet.app.R
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.core.ui.theme.*
import com.luxwallet.app.navigation.LuxDestinations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun HomeScreen(onNavigate: (String) -> Unit = {}) {
    val viewModel = luxViewModel { HomeViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()
    val semantic = LocalLuxSemanticColors.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_launcher_foreground), "Logo dompet Lux Wallet",
                    Modifier.size(48.dp).background(LuxInk, RoundedCornerShape(16.dp)))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("LUX WALLET", style = MaterialTheme.typography.titleMedium)
                    Text("Keuanganmu, dalam kendali.", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { onNavigate(LuxDestinations.SETTINGS) }) { Icon(Icons.Outlined.Settings, "Pengaturan") }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF303129), LuxInk)), RoundedCornerShape(28.dp)).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))).uppercase(), color = LuxGold, style = MaterialTheme.typography.labelMedium)
                Text("Total kekayaan bersih", color = Color(0xFFD3CFC4))
                Text(if (state.isLoading) "Memuat…" else AmountFormat.rupiah(state.netWorth), color = Color.White, style = MaterialTheme.typography.headlineLarge)
                HorizontalDivider(color = Color(0xFF49483E))
                Text("Aset dikurangi kewajiban • saldo estimasi", color = Color(0xFFD3CFC4), style = MaterialTheme.typography.bodySmall)
            }
        }
        item { NotificationStatusCard() }
        item {
            Text("Ruang belanja hari ini", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(AmountFormat.rupiah(state.remainingToday.coerceAtLeast(0)), style = MaterialTheme.typography.headlineMedium)
                    Text("Sisa dari alokasi harian", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(progress = { if (state.safeToSpendToday > 0) (state.todaySpent.toFloat() / state.safeToSpendToday).coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth())
                    StatRow("Sudah terpakai", AmountFormat.rupiah(state.todaySpent))
                    StatRow("Alokasi hari ini", AmountFormat.rupiah(state.safeToSpendToday))
                    Text(if (state.isOverBudget || state.remainingToday < 0) "Pengeluaran melewati alokasi" else "Disesuaikan dengan rencana bulananmu",
                        style = MaterialTheme.typography.bodySmall, color = if (state.isOverBudget) semantic.warning else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Arus kas bulan ini", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyTile("Pemasukan", state.monthIncome, semantic.income, Modifier.weight(1f))
                MoneyTile("Pengeluaran", state.monthExpense, semantic.expense, Modifier.weight(1f))
            }
            TextButton(onClick = { onNavigate(LuxDestinations.TRANSACTIONS) }) { Text("Lihat semua transaksi →") }
        }
        item {
            Text("Sebaran aset", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.assetDistribution.isEmpty()) {
                        Text("Mulai dari saldo yang kamu miliki", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { onNavigate(LuxDestinations.ACCOUNTS) }) { Text("Atur rekening & saldo awal") }
                    } else {
                        DonutChart(state.assetDistribution, centerLabel = AmountFormat.rupiah(state.assetDistribution.sumOf { it.value }.toLong()), centerSubLabel = "Total aset positif")
                        state.assetDistribution.forEach { StatRow(it.label, AmountFormat.rupiah(it.value.toLong())) }
                    }
                }
            }
        }
        items(state.insights) { insight ->
            OutlinedCard(Modifier.fillMaxWidth()) { Text(insight.message, Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
@Composable private fun MoneyTile(label: String, amount: Long, color: Color, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(AmountFormat.rupiah(amount), color = color, style = MaterialTheme.typography.titleMedium)
    } }
}
@Composable private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
