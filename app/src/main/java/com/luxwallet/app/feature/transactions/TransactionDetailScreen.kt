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

@Composable fun TransactionDetailScreen(transactionId: Long) {
    val vm = luxViewModel { TransactionDetailViewModel.create(it, transactionId) }
    val state by vm.uiState.collectAsState()
    val tx = state.transaction ?: run { CircularProgressIndicator(Modifier.padding(24.dp)); return }
    var merchant by remember(tx.id) { mutableStateOf(tx.merchantName.orEmpty()) }
    var note by remember(tx.id) { mutableStateOf(tx.note.orEmpty()) }
    var learn by remember { mutableStateOf(false) }
    val held = tx.reviewReason == ReviewReason.POSSIBLE_DUPLICATE
    val ignored = tx.reviewStatus == ReviewStatus.IGNORED
    val category = state.mainCategories.firstOrNull { it.id == tx.categoryId }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Detail transaksi", style = MaterialTheme.typography.headlineMedium)
        Text(AmountFormat.rupiah(tx.amount), style = MaterialTheme.typography.headlineLarge)
        if (held) OutlinedCard { Text("Notifikasi mirip dengan transaksi yang sudah dicatat. Nominal ini belum dihitung lagi. Pilih Abaikan duplikat jika satu pembayaran, atau Transaksi berbeda untuk menghitungnya.", Modifier.padding(18.dp)) }
        if (ignored) Text("Diabaikan · tidak memengaruhi saldo atau arus kas.")
        OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text("Merchant / penerima") }, enabled = !ignored)
        ChoiceField("Kategori", category?.name ?: "Pilih kategori", state.mainCategories.map { it.name },
            { vm.updateCategory(state.mainCategories[it].id, learn) }, enabled = !ignored)
        if (tx.direction == TransactionDirection.OUT && !tx.isInternalTransfer && !ignored) {
            FilterChip(selected = category?.name == TransportPlan.CATEGORY, onClick = {
                state.mainCategories.firstOrNull { it.name == TransportPlan.CATEGORY }?.let { vm.updateCategory(it.id, false) }
            }, label = { Text("Transportasi rutin · di luar uang belanja bebas") })
        }
        if (!tx.merchantName.isNullOrBlank()) Row {
            Switch(learn, { learn = it }, enabled = !ignored)
            Text("Gunakan kategori ini untuk merchant yang sama", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Catatan") }, enabled = !ignored)
        if (!held && !ignored) Row {
            Switch(tx.isExcludedFromCashflow, { vm.setExcludedFromCashflow(it) })
            Text("Keluarkan dari arus kas", Modifier.padding(12.dp))
        }
        if (!ignored) {
            Button({ vm.saveAndConfirm(merchant, note) }, Modifier.fillMaxWidth()) { Text(if (held) "Ini transaksi berbeda · hitung nominal" else "Simpan & konfirmasi") }
            OutlinedButton({ vm.markIgnored() }, Modifier.fillMaxWidth()) { Text("Abaikan duplikat") }
        }
    }
}
