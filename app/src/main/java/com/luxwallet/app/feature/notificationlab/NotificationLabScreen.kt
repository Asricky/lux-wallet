package com.luxwallet.app.feature.notificationlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.ui.luxViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm:ss")

@Composable
fun NotificationLabScreen() {
    val viewModel = luxViewModel { NotificationLabViewModel.create(it) }
    val observations by viewModel.observations.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.luxwallet.app.LuxWalletApp
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val discovery by app.preferences.packageDiscoveryEnabled.collectAsState(initial = false)
    val packages by app.preferences.discoveredPackages.collectAsState(initial = emptySet())
    val mappings by app.preferences.packageMappings.collectAsState(initial = emptyMap())
    var packageName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var source by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(com.luxwallet.app.core.model.SourceApp.SEABANK) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Diagnostik notifikasi", style = MaterialTheme.typography.headlineMedium)
            com.luxwallet.app.core.ui.component.NotificationStatusCard()
            Text("Temukan nama paket aplikasi. Mode ini hanya menyimpan nama paket, tanpa isi notifikasi aplikasi lain.", style = MaterialTheme.typography.bodySmall)
            androidx.compose.material3.Switch(checked = discovery, onCheckedChange = { scope.launch { app.preferences.setPackageDiscoveryEnabled(it) } })
            Text(packages.sorted().joinToString("\n").ifBlank { "Belum ada paket baru." }, style = MaterialTheme.typography.bodySmall)
            androidx.compose.material3.OutlinedTextField(packageName, { packageName = it.trim() }, label = { Text("Nama paket aplikasi keuangan") }, modifier = Modifier.fillMaxWidth())
            com.luxwallet.app.core.model.SourceApp.entries.forEach { option ->
                androidx.compose.material3.FilterChip(selected = source == option, onClick = { source = option }, label = { Text(option.name) })
            }
            androidx.compose.material3.Button(enabled = packageName.matches(Regex("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+")), onClick = {
                scope.launch { app.preferences.mapPackage(packageName, source); packageName = "" }
            }) { Text("Izinkan paket ini sebagai sumber transaksi") }
            mappings.forEach { (pkg, mapped) ->
                androidx.compose.material3.TextButton(onClick = { scope.launch { app.preferences.unmapPackage(pkg) } }) { Text("$pkg → $mapped · Hapus") }
            }
        }
        items(observations, key = { it.id }) { obs ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${obs.sourceApp} • ${obs.parseStatus}", style = MaterialTheme.typography.titleMedium)
                    Text("package: ${obs.packageName}", style = MaterialTheme.typography.bodyMedium)
                    Text("title: ${obs.title}", style = MaterialTheme.typography.bodyMedium)
                    Text("text: ${obs.text}", style = MaterialTheme.typography.bodyMedium)
                    obs.bigText?.let { Text("bigText: $it", style = MaterialTheme.typography.bodyMedium) }
                    obs.subText?.let { Text("subText: $it", style = MaterialTheme.typography.bodyMedium) }
                    obs.textLines?.let { Text("textLines: $it", style = MaterialTheme.typography.bodyMedium) }
                    Text(
                        "posted: ${Instant.ofEpochMilli(obs.postedAt).atZone(ZoneId.systemDefault()).format(FORMAT)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("hash: ${obs.rawPayloadHash.take(12)}…  parserVersion: ${obs.parserVersion}", style = MaterialTheme.typography.bodyMedium)
                    obs.parseFailureReason?.let { Text("failure: $it", style = MaterialTheme.typography.bodyMedium) }
                    Text("linked transaction: ${obs.linkedTransactionId ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (observations.isEmpty()) {
            item { Text("No notifications captured yet.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
