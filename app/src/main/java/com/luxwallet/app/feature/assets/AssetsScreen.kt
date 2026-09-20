@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.luxwallet.app.feature.assets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.model.AssetClass
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.parser.core.AmountParser
import kotlinx.coroutines.launch

@Composable fun AssetsScreen(onAccounts: () -> Unit = {}) {
    val vm = luxViewModel { AssetsViewModel.create(it) }
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()
    val error by vm.error.collectAsState()
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(0) }
    var edit by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var assetClass by rememberSaveable { mutableStateOf(AssetClass.OTHER) }
    val account = edit?.takeIf { it.startsWith("account:") }?.substringAfter(':')?.toLongOrNull()?.let { id -> state.liquidAccounts.find { it.id == id } }
    val asset = edit?.takeIf { it.startsWith("asset:") }?.substringAfter(':')?.toLongOrNull()?.let { id -> (state.investments + state.otherAssets).find { it.id == id } }
    val liability = if (edit == "newDebt") com.luxwallet.app.core.database.entity.LiabilityEntity(name = "", type = com.luxwallet.app.core.model.LiabilityType.OTHER_DEBT, currentOutstanding = 0, updatedAt = System.currentTimeMillis()) else edit?.takeIf { it.startsWith("debt:") }?.substringAfter(':')?.toLongOrNull()?.let { id -> state.liabilities.find { it.id == id } }
    fun money(value: Long) = if (hidden) "********" else AmountFormat.rupiah(value)
    fun open(key: String, title: String, value: Long, type: AssetClass = AssetClass.OTHER) {
        edit = key; name = title; amount = value.toString(); assetClass = type; vm.error.value = null
    }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("Aset saya", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Kekayaan bersih", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton({ scope.launch { app.preferences.setAmountsHidden(!hidden) } }) { Icon(if (hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Tampilkan atau sembunyikan saldo") }
                }
                Text(money(state.netWorth), style = MaterialTheme.typography.headlineLarge)
                Text("Aset ${money(state.totalAssets)}", style = MaterialTheme.typography.bodyMedium)
                Text("Kewajiban ${money(state.totalLiabilities)}", style = MaterialTheme.typography.bodyMedium)
            } }
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alokasi aset", style = MaterialTheme.typography.titleMedium)
                    listOf("Simpanan" to state.liquidAccounts.filter { it.includeInNetWorth }.sumOf { it.currentEstimatedBalance },
                        "Investasi" to state.investments.filter { it.includeInNetWorth }.sumOf { it.currentValue },
                        "Aset lainnya" to state.otherAssets.filter { it.includeInNetWorth }.sumOf { it.currentValue }).forEach { (label, value) ->
                        Row(Modifier.fillMaxWidth()) { Text(label, Modifier.weight(1f)); Text(money(value)) }
                    }
                }
            }
        }
        item {
            ScrollableTabRow(tab, edgePadding = 0.dp) { listOf("Simpanan", "Investasi", "Lainnya", "Utang").forEachIndexed { index, label ->
                Tab(tab == index, { tab = index }, text = { Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) })
            } }
        }
        when (tab) {
            0 -> {
                items(state.liquidAccounts, key = { it.id }) { item ->
                    AssetRow(item.name, "Saldo estimasi · ketuk untuk menyesuaikan", money(item.currentEstimatedBalance)) { open("account:${item.id}", item.name, item.currentEstimatedBalance) }
                }
                item { OutlinedButton(onAccounts, Modifier.fillMaxWidth()) { Text("Kelola / tambah rekening") } }
            }
            1, 2 -> {
                val assets = if (tab == 1) state.investments else state.otherAssets
                items(assets, key = { it.id }) { item ->
                    AssetRow(item.name, item.assetClass.label(), money(item.currentValue)) { open("asset:${item.id}", item.name, item.currentValue, item.assetClass) }
                }
                item { OutlinedButton({ open("new", "", 0, if (tab == 1) AssetClass.REKSA_DANA else AssetClass.OTHER) }, Modifier.fillMaxWidth()) { Text("Tambah aset manual") } }
            }
            3 -> {
                items(state.liabilities, key = { it.id }) { item ->
                    AssetRow(item.name, "Sisa kewajiban · ubah manual", money(item.currentOutstanding)) { open("debt:${item.id}", item.name, item.currentOutstanding) }
                }
                item { OutlinedButton({ open("newDebt", "", 0) }, Modifier.fillMaxWidth()) { Text("Tambah utang") } }
            }
        }
        item { Text("Ketuk aset untuk mengubah nominal. Perubahan saldo rekening dicatat sebagai penyesuaian, sehingga tidak menjadi pemasukan atau pengeluaran.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (edit != null) ModalBottomSheet(onDismissRequest = { if (!saving) edit = null }) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (edit == "new") "Tambah aset" else if (edit == "newDebt") "Tambah utang" else "Ubah nominal", style = MaterialTheme.typography.titleLarge)
            if (account != null) Text(account.name, style = MaterialTheme.typography.titleMedium)
            else OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Nama") }, singleLine = true)
            if (account == null && liability == null) {
                val types = AssetClass.entries.filter { it !in setOf(AssetClass.BANK, AssetClass.EWALLET, AssetClass.CASH) }
                ChoiceField("Jenis aset", assetClass.label(), types.map { it.label() }, { assetClass = types[it] })
            }
            MoneyField(if (liability != null) "Sisa utang" else "Nilai saat ini", amount, { amount = it })
            if (account != null) Text("Selisih terhadap saldo sekarang akan dicatat sebagai koreksi saldo.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                val value = AmountParser.normalizeOrNull(amount)
                if (value == null || value < 0) vm.error.value = "Isi nominal nol atau positif yang valid."
                else vm.saveValue(account?.id, asset, liability, name, assetClass, value) { edit = null }
            }) { Text(if (saving) "Menyimpan…" else "Simpan nilai") }
        }
    }
}
private fun AssetClass.label() = when (this) {
    AssetClass.REKSA_DANA -> "Reksa dana"; AssetClass.OBLIGASI_SBN -> "Obligasi / SBN"; AssetClass.SAHAM -> "Saham"
    AssetClass.DEPOSITO -> "Deposito"; AssetClass.EMAS -> "Emas"; AssetClass.KENDARAAN -> "Kendaraan"
    AssetClass.PROPERTI -> "Properti"; AssetClass.PIUTANG -> "Piutang"; AssetClass.CRYPTO -> "Kripto"
    else -> "Aset lainnya"
}
@Composable private fun AssetRow(name: String, subtitle: String, value: String, onEdit: () -> Unit) {
    Card(onEdit, Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary); Text(name, style = MaterialTheme.typography.titleMedium) }
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Ubah nominal", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge); Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp)) }
        }
    }
}
