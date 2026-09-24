package com.luxwallet.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.ui.component.NotificationStatusCard
import kotlinx.coroutines.launch

@Composable fun NotificationSettingsScreen(onDiagnostics: () -> Unit) {
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val sources by app.preferences.enabledSources.collectAsState(initial = emptySet())
    val diagnostics by app.preferences.listenerDiagnostics.collectAsState(initial = 0L to 0L)
    fun time(at: Long) = if (at == 0L) "Belum ada" else java.text.SimpleDateFormat("d MMM, HH:mm:ss", java.util.Locale("id", "ID")).format(java.util.Date(at))
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Notifikasi", style = MaterialTheme.typography.headlineMedium) }
        item { NotificationStatusCard() }
        item { OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Aktivitas pemantau", style = MaterialTheme.typography.titleMedium)
            Text("Notifikasi terakhir: ${time(diagnostics.first)}", style = MaterialTheme.typography.bodySmall)
            Text("Parser terakhir: ${time(diagnostics.second)}", style = MaterialTheme.typography.bodySmall)
            Text("Pemulihan berjalan saat koneksi terputus, proses dimulai, perangkat menyala, atau aplikasi diperbarui. Setelah Paksa berhenti, buka aplikasi kembali.", style = MaterialTheme.typography.bodySmall)
        } } }
        item { Text("Sumber transaksi", style = MaterialTheme.typography.titleMedium) }
        item {
            Card {
                Column(Modifier.padding(18.dp)) {
                    SourceApp.entries.forEach { source ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(when (source) { SourceApp.MYBCA -> "BCA mobile / myBCA"; SourceApp.SEABANK -> "SeaBank"; SourceApp.SHOPEEPAY -> "ShopeePay"; SourceApp.GOPAY -> "GoPay" }, Modifier.weight(1f))
                            Switch(source in sources, { enabled -> scope.launch { app.preferences.setSourceEnabled(source, enabled) } })
                        }
                    }
                }
            }
        }
        item { Text("Jika transaksi muncul dua kali, buka Perlu ditinjau. Calon duplikat tidak menambah pengeluaran sampai kamu menyatakannya sebagai transaksi berbeda.", style = MaterialTheme.typography.bodyMedium) }
        item { OutlinedButton(onDiagnostics, Modifier.fillMaxWidth()) { Text("Lihat diagnostik notifikasi") } }
    }
}
