package com.luxwallet.app.feature.transactions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.model.*
import com.luxwallet.app.core.ui.component.ChoiceField
import com.luxwallet.app.core.ui.luxViewModel

@Composable fun TransactionDetailScreen(transactionId: Long, onDone: () -> Unit = {}) {
    val vm = luxViewModel { TransactionDetailViewModel.create(it, transactionId) }
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()
    val error by vm.error.collectAsState()
    val tx = state.transaction ?: run { if (state.loaded) Text("Transaksi tidak ditemukan", Modifier.padding(24.dp)) else CircularProgressIndicator(Modifier.padding(24.dp)); return }
    var selectedAccount by androidx.compose.runtime.saveable.rememberSaveable(tx.id) { mutableStateOf(tx.sourceAccountId) }
    var selectedCategory by androidx.compose.runtime.saveable.rememberSaveable(tx.id) { mutableStateOf(tx.categoryId) }
    var excluded by androidx.compose.runtime.saveable.rememberSaveable(tx.id) { mutableStateOf(tx.isExcludedFromCashflow && tx.reviewReason != ReviewReason.ACCOUNT_NOT_CONFIGURED) }
    androidx.activity.compose.BackHandler(saving) { }

    var merchant by androidx.compose.runtime.saveable.rememberSaveable(tx.id) { mutableStateOf(tx.merchantName.orEmpty()) }
    var note by androidx.compose.runtime.saveable.rememberSaveable(tx.id) { mutableStateOf(tx.note.orEmpty()) }
    var learn by remember { mutableStateOf(false) }
    val held = tx.reviewReason == ReviewReason.POSSIBLE_DUPLICATE
    val ignored = tx.reviewStatus == ReviewStatus.IGNORED
    val category = state.mainCategories.firstOrNull { it.id == selectedCategory }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Detail transaksi", style = MaterialTheme.typography.headlineMedium)
        Text(com.luxwallet.app.core.common.privateRupiah(tx.amount), style = MaterialTheme.typography.headlineLarge)
        if (held) OutlinedCard { Text("Notifikasi mirip dengan transaksi yang sudah dicatat. Nominal ini belum dihitung lagi. Pilih Abaikan duplikat jika satu pembayaran, atau Transaksi berbeda untuk menghitungnya.", Modifier.padding(18.dp)) }
        if (ignored) Text("Diabaikan · tidak memengaruhi saldo atau arus kas.")
        if (tx.sourceAccountId == null) ChoiceField("Rekening", state.accounts.find { it.id == selectedAccount }?.name ?: "Pilih rekening",
            state.accounts.map { it.name }, { selectedAccount = state.accounts[it].id }, enabled = !ignored && !saving)
        OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text("Merchant / penerima") }, enabled = !ignored && !saving)
        ChoiceField("Kategori", category?.name ?: "Pilih kategori", state.mainCategories.map { it.name },
            { selectedCategory = state.mainCategories[it].id }, enabled = !ignored && !saving)
        if (tx.direction == TransactionDirection.OUT && !tx.isInternalTransfer && !ignored) {
            FilterChip(selected = category?.name == TransportPlan.CATEGORY, onClick = {
                state.mainCategories.firstOrNull { it.name == TransportPlan.CATEGORY }?.let { selectedCategory = it.id }
            }, label = { Text("Transportasi rutin · di luar uang belanja bebas") })
        }
        if (!tx.merchantName.isNullOrBlank()) Row {
            Switch(learn, { learn = it }, enabled = !ignored && !saving)
            Text("Gunakan kategori ini untuk merchant yang sama", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Catatan") }, enabled = !ignored && !saving)
        if (!held && !ignored) Row {
            Switch(excluded, { excluded = it }, enabled = !saving)
            Text("Keluarkan dari arus kas", Modifier.padding(12.dp))
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!ignored) {
            Button({ vm.saveAndConfirm(merchant, note, selectedCategory, excluded, learn, selectedAccount, onDone) }, Modifier.fillMaxWidth(), enabled = !saving) { Text(if (held) "Ini transaksi berbeda · hitung nominal" else "Simpan & konfirmasi") }
            OutlinedButton({ vm.markIgnored(onDone) }, Modifier.fillMaxWidth(), enabled = !saving) { Text("Abaikan duplikat") }
        }
    }
}
