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
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Notifikasi", style = MaterialTheme.typography.headlineMedium) }
        item { NotificationStatusCard() }
        item { Text("Sumber transaksi", style = MaterialTheme.typography.titleMedium) }
        item {
            Card {
                Column(Modifier.padding(18.dp)) {
                    SourceApp.entries.forEach { source ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(when (source) { SourceApp.MYBCA -> "myBCA"; SourceApp.SEABANK -> "SeaBank"; SourceApp.SHOPEEPAY -> "ShopeePay"; SourceApp.GOPAY -> "GoPay" }, Modifier.weight(1f))
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
