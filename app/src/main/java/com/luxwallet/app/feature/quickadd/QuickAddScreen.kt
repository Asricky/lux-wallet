package com.luxwallet.app.feature.quickadd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.parser.core.AmountParser

@Composable fun QuickAddScreen(onDone: () -> Unit = {}, onAccounts: () -> Unit = {}) {
    val viewModel = luxViewModel { QuickAddViewModel.create(it) }
    val options by viewModel.options.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val saveError by viewModel.error.collectAsState()
    var kind by rememberSaveable { mutableStateOf(QuickAddKind.EXPENSE) }
    var amount by rememberSaveable { mutableStateOf("") }
    var accountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var destinationId by rememberSaveable { mutableStateOf<Long?>(null) }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var merchant by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var increases by rememberSaveable { mutableStateOf(true) }
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val kinds = listOf("Pengeluaran", "Pemasukan", "Pindah saldo / top-up sendiri", "Koreksi saldo (+/−)")
    val source = options.accounts.firstOrNull { it.id == accountId }
    val destination = options.accounts.firstOrNull { it.id == destinationId }
    val category = options.categories.firstOrNull { it.id == categoryId }
    val parsed = AmountParser.normalizeOrNull(amount)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(if (reviewing) "Periksa catatan" else "Catat transaksi", style = MaterialTheme.typography.headlineMedium)
        Text(if (reviewing) "2 dari 2 · Konfirmasi" else "1 dari 2 · Detail transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = { if (reviewing) 1f else 0.5f }, modifier = Modifier.fillMaxWidth())
        if (options.accounts.isEmpty()) {
            Text("Buat rekening atau dompet tunai dulu agar transaksi punya sumber saldo.")
            Button(onAccounts, Modifier.fillMaxWidth()) { Text("Tambah rekening") }
        } else if (!reviewing) {
            ChoiceField("Jenis catatan", kinds[kind.ordinal], kinds, { kind = QuickAddKind.entries[it]; error = null })
            MoneyField("Nominal", amount, { amount = it; error = null })
            ChoiceField(if (kind == QuickAddKind.TRANSFER) "Dari rekening" else "Rekening", source?.name ?: "Pilih rekening",
                options.accounts.map { it.name }, { accountId = options.accounts[it].id; if (destinationId == accountId) destinationId = null })
            if (kind == QuickAddKind.TRANSFER) {
                val destinations = options.accounts.filter { it.id != accountId }
                ChoiceField("Ke rekening / e-wallet sendiri", destination?.name ?: "Pilih tujuan", destinations.map { it.name }, { destinationId = destinations[it].id })
                Text("Top-up berkala dicatat di sini. Saldo berpindah antar akunmu; bukan pengeluaran dan tidak mengurangi ruang belanja.", style = MaterialTheme.typography.bodySmall)
            }
            if (kind == QuickAddKind.EXPENSE || kind == QuickAddKind.INCOME) {
                ChoiceField("Kategori", category?.name ?: "Pilih kategori", options.categories.map { it.name }, { categoryId = options.categories[it].id })
                if (kind == QuickAddKind.EXPENSE) {
                    FilterChip(selected = category?.name == TransportPlan.CATEGORY, onClick = {
                        categoryId = options.categories.firstOrNull { it.name == TransportPlan.CATEGORY }?.id
                    }, label = { Text("Transportasi rutin · di luar uang belanja bebas") })
                }
                OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text("Merchant / keterangan (opsional)") }, singleLine = true)
            }
            if (kind == QuickAddKind.BALANCE_ADJUSTMENT) ChoiceField("Arah koreksi", if (increases) "Menambah saldo" else "Mengurangi saldo",
                listOf("Menambah saldo", "Mengurangi saldo"), { increases = it == 0 })
            OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Catatan tambahan (opsional)") })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                error = when {
                    parsed == null || parsed <= 0 -> "Isi nominal rupiah yang valid dan lebih dari nol."
                    source == null -> "Pilih rekening sumber."
                    kind == QuickAddKind.TRANSFER && (destination == null || destinationId == accountId) -> "Pilih rekening tujuan yang berbeda."
                    (kind == QuickAddKind.EXPENSE || kind == QuickAddKind.INCOME) && category == null -> "Pilih kategori agar catatanmu rapi."
                    else -> null
                }
                if (error == null) reviewing = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Lanjut ke ringkasan") }
        } else {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(kinds[kind.ordinal], style = MaterialTheme.typography.titleMedium)
                Text(AmountFormat.rupiah(parsed ?: 0), style = MaterialTheme.typography.headlineLarge)
                Text("Rekening: ${source?.name ?: "Tidak tersedia"}")
                if (kind == QuickAddKind.TRANSFER) Text("Tujuan: ${destination?.name ?: "Tidak tersedia"}")
                else if (category != null) Text("Kategori: ${category.name}")
                if (merchant.isNotBlank()) Text(merchant)
                if (note.isNotBlank()) Text(note)
                Text("Tanggal: hari ini", style = MaterialTheme.typography.bodySmall)
            } }
            saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = !saving && source != null && parsed != null && parsed > 0, modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.submit(kind, parsed ?: 0, source!!.id, destinationId, categoryId,
                    merchant.ifBlank { null }, note.ifBlank { null }, increases, onDone)
            }) { Text(if (saving) "Menyimpan…" else "Simpan transaksi") }
            OutlinedButton(enabled = !saving, onClick = { reviewing = false }, modifier = Modifier.fillMaxWidth()) { Text("Kembali & ubah") }
        }
    }
}
