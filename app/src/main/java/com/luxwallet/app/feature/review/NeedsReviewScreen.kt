package com.luxwallet.app.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.privateRupiah
import com.luxwallet.app.core.ui.luxViewModel
import kotlinx.coroutines.launch

@Composable fun NeedsReviewScreen(onTransactionClick: (Long) -> Unit = {}, onManual: () -> Unit = {}) {
    val vm = luxViewModel { NeedsReviewViewModel.create(it) }
    val state by vm.uiState.collectAsState()
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val observations by app.notificationRepository.observeAll().collectAsState(initial = emptyList())
    val failed = observations.filter { it.parseStatus == com.luxwallet.app.core.model.ParseStatus.FAILED }
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf<Pair<Boolean, Long>?>(null) }
    var evidence by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm", java.util.Locale("id", "ID")).withZone(java.time.ZoneId.systemDefault()) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Perlu ditinjau", style = MaterialTheme.typography.headlineSmall) }
        item { Text("${state.transactions.size + failed.size} catatan menunggu keputusan", style = MaterialTheme.typography.bodyMedium) }
        error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        items(state.transactions, key = { "tx-${it.id}" }) { tx ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(privateRupiah(tx.amount), style = MaterialTheme.typography.titleLarge)
                Text("${state.accountNames[tx.sourceAccountId] ?: "Rekening belum diatur"} · ${dateFormat.format(java.time.Instant.ofEpochMilli(tx.transactionTime))}", style = MaterialTheme.typography.bodySmall)
                Text(state.categoryNames[tx.categoryId] ?: "Kategori belum jelas", style = MaterialTheme.typography.bodyMedium)
                if (tx.reviewReason == com.luxwallet.app.core.model.ReviewReason.POSSIBLE_DUPLICATE) Text("Calon duplikat · belum dihitung", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton({ onTransactionClick(tx.id) }, enabled = !busy) { Text("Tinjau") }
                    TextButton({ deleting = false to tx.id }, enabled = !busy) { Text("Hapus") }
                }
            } }
        }
        items(failed, key = { "obs-${it.id}" }) { item ->
            OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text("${item.sourceApp} · Belum dikenali", style = MaterialTheme.typography.titleSmall)
                Text(dateFormat.format(java.time.Instant.ofEpochMilli(item.postedAt)), style = MaterialTheme.typography.bodySmall)
                Row {
                    TextButton({ evidence = item.bigText ?: item.text }, enabled = !busy) { Text("Tinjau") }
                    TextButton({ deleting = true to item.id }, enabled = !busy) { Text("Hapus") }
                }
            } }
        }
        if (state.transactions.isEmpty() && failed.isEmpty() && !state.isLoading) item { Text("Semua sudah ditinjau.") }
    }
    evidence?.let { text -> AlertDialog(onDismissRequest = { evidence = null }, title = { Text("Notifikasi belum dikenali") },
        text = { Text(text + "\n\nBelum masuk saldo. Catat manual hanya jika ini transaksi valid.") },
        confirmButton = { TextButton({ evidence = null; onManual() }) { Text("Catat manual") } },
        dismissButton = { TextButton({ evidence = null }) { Text("Tutup") } }) }
    deleting?.let { (observation, id) -> AlertDialog(onDismissRequest = { if (!busy) deleting = null }, title = { Text("Hapus dari daftar tinjauan?") },
        text = { Text("Catatan diabaikan. Jika nominal sudah dihitung, pengaruhnya pada saldo dibalik. Histori tetap disimpan.") },
        confirmButton = { TextButton(enabled = !busy, onClick = { busy = true; scope.launch {
            try { if (observation) app.notificationRepository.dismissFailed(id) else app.transactionRepository.markIgnored(id); deleting = null }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { error = "Belum berhasil dihapus. Coba lagi." }
            finally { busy = false }
        } }) { Text("Hapus catatan") } }, dismissButton = { TextButton({ deleting = null }, enabled = !busy) { Text("Batal") } }) }
}
