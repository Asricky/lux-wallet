package com.luxwallet.app.feature.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.model.*
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel

@Composable fun OnboardingScreen() {
    val vm = luxViewModel { OnboardingViewModel.create(it) }
    val saving by vm.saving.collectAsState()
    val error by vm.error.collectAsState()
    val context = LocalContext.current
    var profileName by rememberSaveable { mutableStateOf("") }
    var step by rememberSaveable { mutableStateOf(0) }
    var sources by rememberSaveable { mutableStateOf(SourceApp.entries.map { it.name }.toTypedArray()) }
    var bca by rememberSaveable { mutableStateOf("") }
    var seabank by rememberSaveable { mutableStateOf("") }
    var gopay by rememberSaveable { mutableStateOf("") }
    var shopee by rememberSaveable { mutableStateOf("") }
    var cash by rememberSaveable { mutableStateOf("") }
    var income by rememberSaveable { mutableStateOf("") }
    var bills by rememberSaveable { mutableStateOf("") }
    var savings by rememberSaveable { mutableStateOf("1000000") }
    var investment by rememberSaveable { mutableStateOf("1500000") }
    BackHandler(step > 0) { if (!saving) step-- }
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) IconButton({ step-- }, enabled = !saving) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali") }
            Text("${step + 1} dari 4", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth())
        when (step) {
            0 -> {
                Lumi(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally))
                Text("Kenali uangmu bersama Lumi", style = MaterialTheme.typography.headlineMedium)
                Text("Lumi mencatat transaksi dari notifikasi dan membantumu merencanakan uang sampai gajian.")
                Text("Data disimpan di perangkat. Tidak meminta login bank atau melakukan pembayaran. Kamu tetap bisa mencatat manual.")
                OutlinedTextField(profileName, { profileName = it.take(40) }, Modifier.fillMaxWidth(), label = { Text("Nama panggilan (opsional)") }, singleLine = true)
                Button({ step++ }, Modifier.fillMaxWidth()) { Text("Mulai") }
            }
            1 -> {
                Text("Pilih sumber transaksi", style = MaterialTheme.typography.headlineMedium)
                SourceApp.entries.forEach { source ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(source.name in sources, { checked -> sources = if (checked) (sources.toList() + source.name).distinct().toTypedArray() else sources.filter { it != source.name }.toTypedArray() })
                        Text(source.name)
                    }
                }
                Text("Akses notifikasi diperlukan untuk pencatatan otomatis. Bisa diaktifkan sekarang atau nanti dari ikon lonceng di Beranda.")
                OutlinedButton({ context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, Modifier.fillMaxWidth()) { Text("Buka akses notifikasi") }
                Button({ step++ }, Modifier.fillMaxWidth()) { Text("Lanjut") }
            }
            2 -> {
                Text("Saldo saat ini", style = MaterialTheme.typography.headlineMedium)
                Text("Isi rekening yang kamu gunakan. Kosongkan yang belum ingin ditambahkan. Aset investasi dan utang bisa diatur nanti melalui menu Aset.")
                MoneyField("BCA", bca, { bca = it })
                MoneyField("SeaBank", seabank, { seabank = it })
                MoneyField("GoPay", gopay, { gopay = it })
                MoneyField("ShopeePay", shopee, { shopee = it })
                MoneyField("Tunai", cash, { cash = it })
                Button({ step++ }, Modifier.fillMaxWidth()) { Text("Lanjut") }
            }
            3 -> {
                Text("Target bulananmu", style = MaterialTheme.typography.headlineMedium)
                Text("Bisa diubah nanti. Target tidak memindahkan uang; rencana sampai gajian dibuat terpisah dari saldo yang benar-benar tersedia.")
                MoneyField("Pendapatan per bulan (opsional)", income, { income = it })
                MoneyField("Kewajiban tetap (opsional)", bills, { bills = it })
                MoneyField("Tabungan modal bisnis", savings, { savings = it })
                MoneyField("Investasi", investment, { investment = it })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                    val balances = listOf(AccountProvider.BCA to bca, AccountProvider.SEABANK to seabank, AccountProvider.GOPAY to gopay, AccountProvider.SHOPEEPAY to shopee, AccountProvider.CASH to cash)
                        .mapNotNull { (provider, value) -> value.toLongOrNull()?.let { provider to it } }.toMap()
                    vm.finish(balances, sources.map { SourceApp.valueOf(it) }.toSet(), income.toLongOrNull() ?: 0, bills.toLongOrNull() ?: 0,
                        savings.toLongOrNull() ?: 0, investment.toLongOrNull() ?: 0, profileName)
                }) { Text(if (saving) "Menyimpan…" else "Simpan & buka Beranda") }
            }
        }
    }
}
