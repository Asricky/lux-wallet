package com.luxwallet.app.feature.coach

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.engine.MoneyCoach
import com.luxwallet.app.feature.planner.PlannerViewModel
import com.luxwallet.app.navigation.LuxDestinations
import com.luxwallet.app.notification.CoachNotifications
import kotlinx.coroutines.launch

@Composable fun CoachScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    val app = context.applicationContext as LuxWalletApp
    val scope = rememberCoroutineScope()
    val vm = luxViewModel { PlannerViewModel(it) }
    val state by vm.state.collectAsState()
    val enabled by app.preferences.coachEnabled.collectAsState(initial = false)
    val mood by app.lumiState.mood.collectAsState()
    var showPreferences by rememberSaveable { mutableStateOf(false) }
    var showInvestments by rememberSaveable { mutableStateOf(false) }
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    fun money(value: Long) = if (hidden) "********" else com.luxwallet.app.core.common.AmountFormat.rupiah(value)
    val icon by app.preferences.lumiIcon.collectAsState(initial = "CALM")
    var allowed by remember { mutableStateOf(CoachNotifications.allowed(context)) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) allowed = CoachNotifications.allowed(context) }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed = CoachNotifications.allowed(context)
        scope.launch { app.preferences.setCoachEnabled(granted && allowed) }
        if (!granted) feedback = "Izin belum diberikan. Saran tetap bisa dibaca di halaman ini."
    }
    val advice = MoneyCoach.advise(state.status, state.today)
    val uri = LocalUriHandler.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Lumi(mood, Modifier.size(88.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Saran Lumi", style = MaterialTheme.typography.labelLarge)
                    Text(when (mood) {
                        LumiMood.CURIOUS -> "Ada catatan yang perlu dicek"
                        LumiMood.SHOCKED -> "Ada pengeluaran besar"
                        LumiMood.EXCITED -> "Pemasukan baru tercatat"
                        LumiMood.PROUD -> "Langkah baik untuk tujuanmu"
                        LumiMood.NERVOUS, LumiMood.FOCUS -> "Budget mulai menipis"
                        LumiMood.SAD -> "Yuk, sesuaikan belanja hari ini"
                        LumiMood.ANGRY -> "Dahulukan kebutuhan utama"
                        LumiMood.HAPPY -> "Masih aman hari ini"
                        else -> "Satu langkah setiap hari"
                    }, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(state.adaptive?.let { "Aman dibelanjakan ${money(it.safeToSpend)}" } ?: "Konfirmasi saldo untuk menyiapkan rencana.", style = MaterialTheme.typography.titleMedium)
            state.status?.takeIf { !it.expired && !hidden }?.let {
                LinearProgressIndicator(progress = { (it.usedPercent / 100).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${it.usedPercent.toInt()}% budget harian terpakai", style = MaterialTheme.typography.bodySmall)
            }
            TextButton({ onNavigate(if (mood == LumiMood.CURIOUS) LuxDestinations.NEEDS_REVIEW else LuxDestinations.PLANNER) }) {
                Text(if (mood == LumiMood.CURIOUS) "Tinjau catatan" else "Tinjau rencana")
            }
        } } }
        state.interim?.let { budget -> item { InterimBudgetCard(budget, hidden) } }
        state.adaptive?.let { budget ->
            item { FinancialMetric("${budget.daysRemaining} hari menuju pemasukan berikutnya", "Budget tersisa ${money(budget.remaining)}", Modifier.fillMaxWidth()) }
            item { FinancialMetric("Cadangan penyangga", money(budget.bufferRemaining), Modifier.fillMaxWidth()) }
        }
        item { TextButton({ showPreferences = !showPreferences }) { Text(if (showPreferences) "Tutup pengaturan Lumi" else "Pengingat & ikon Lumi") } }
        if (showPreferences) {
        item { OutlinedCard { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pengingat harian", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Switch(checked = enabled && allowed, onCheckedChange = { value ->
                    if (!value) scope.launch { app.preferences.setCoachEnabled(false) }
                    else if (Build.VERSION.SDK_INT >= 33 && !allowed) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else if (allowed) scope.launch { app.preferences.setCoachEnabled(true) }
                    else feedback = "Notifikasi diblokir perangkat. Buka izin notifikasi di bawah."
                })
            }
            Text("Maksimal sekali sehari, pukul 09.00–21.00 waktu perangkat. Waktu kirim mengikuti penghematan baterai Android; tidak selalu tepat pukul 09.00.", style = MaterialTheme.typography.bodySmall)
            Text("Isi pengingat tidak mencantumkan nominal. Tampilan layar kunci memakai pesan umum.", style = MaterialTheme.typography.bodySmall)
            TextButton({ context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Izin notifikasi perangkat") }
            OutlinedButton(enabled = allowed, onClick = {
                feedback = if (CoachNotifications.send(context, advice)) "Contoh notifikasi terkirim." else "Notifikasi belum dapat dikirim."
            }) { Text("Kirim contoh notifikasi") }
            feedback?.let { Text(it) }
        } } }
        item {
            val values = listOf("AUTO", "HAPPY", "PROUD", "CALM", "EXCITED", "CURIOUS", "NERVOUS", "SHOCKED", "SAD", "ANGRY")
            val labels = listOf("Otomatis mengikuti keuangan", "Senang", "Bangga", "Tenang", "Antusias", "Penasaran", "Waspada", "Terkejut", "Sedih", "Peringatan")
            ChoiceField("Ikon aplikasi", labels[values.indexOf(icon).coerceAtLeast(0)], labels, { index ->
                scope.launch {
                    try {
                        val chosenMood = if (values[index] == "AUTO") mood else LumiMood.valueOf(values[index])
                        LumiLauncher.apply(context, chosenMood)
                        app.preferences.setLumiIcon(values[index])
                        feedback = "Ikon diperbarui. Launcher perangkat mungkin membutuhkan waktu untuk menyegarkan ikon."
                    } catch (_: Exception) { feedback = "Launcher belum menerima perubahan ikon. Coba kembali." }
                }
            })
            Text("Mode otomatis mengikuti transaksi dan rencana selama proses aplikasi berjalan. Launcher perangkat dapat menunda penyegaran ikon.", style = MaterialTheme.typography.bodySmall)
        }
        }
        item { TextButton({ showInvestments = !showInvestments }) { Text(if (showInvestments) "Tutup pilihan investasi" else "Pilihan investasi sesuai tujuan") } }
        if (showInvestments) {
        item { Text("Kebutuhan dekat & modal bisnis\nUtamakan tabungan yang mudah dicairkan. Reksa dana pasar uang memiliki risiko dan bukan simpanan yang dijamin LPS. Jangan memasukkan uang makan sampai gajian ke instrumen yang sulit dicairkan.") }
        item { Text("Dana beberapa tahun\nPertimbangkan SBN ritel dengan tenor sesuai tujuan. Cek masa penawaran, pajak, ketentuan pencairan, dan risiko harga bila dijual sebelum jatuh tempo.") }
        item { Text("Tujuan lebih dari 5 tahun\nReksa dana indeks terdiversifikasi bisa dipertimbangkan jika siap menerima penurunan nilai. Bandingkan biaya dan prospektus. Investasi kembali hasilnya; imbal hasil tidak tetap atau dijamin.") }
        item {
            TextButton({ uri.openUri("https://www.ojk.go.id/Files/box/BukuSakuOJK.pdf") }) { Text("Baca jenis & risiko investasi · OJK") }
            TextButton({ uri.openUri("https://www.kemenkeu.go.id/sukukritel") }) { Text("Ketentuan SBN ritel · Kemenkeu") }
            TextButton({ onNavigate(LuxDestinations.CALCULATOR) }) { Text("Simulasikan rencana investasi") }
        }
        }
    }
}
