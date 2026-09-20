package com.luxwallet.app.feature.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.engine.PaydayPlan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val planDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))

@Composable fun PlannerScreen() {
    val vm = luxViewModel { PlannerViewModel(it) }
    val state by vm.state.collectAsState()
    val saving by vm.saving.collectAsState()
    val message by vm.message.collectAsState()
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    val transport by app.preferences.transportPlan.collectAsState(initial = com.luxwallet.app.core.common.TransportPlan())
    var editing by rememberSaveable { mutableStateOf(false) }
    var cash by rememberSaveable { mutableStateOf("") }
    var bills by rememberSaveable { mutableStateOf("0") }
    var buffer by rememberSaveable { mutableStateOf("0") }
    var business by rememberSaveable { mutableStateOf("1000000") }
    var investment by rememberSaveable { mutableStateOf("1500000") }
    var pay25 by rememberSaveable { mutableStateOf("") }
    var pay1 by rememberSaveable { mutableStateOf("") }
    var daily by rememberSaveable { mutableStateOf("6000") }
    var weekdays by rememberSaveable { mutableStateOf(true) }
    var payday by rememberSaveable { mutableStateOf(PaydayPlan.nextPayday(LocalDate.now()).toEpochDay()) }
    var error by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(editing) { if (!saving) discard = true }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Batalkan perubahan rencana?") },
        text = { Text("Rencana sebelumnya tetap tersimpan.") },
        confirmButton = { TextButton({ discard = false; editing = false }) { Text("Batalkan perubahan") } },
        dismissButton = { TextButton({ discard = false }) { Text("Lanjut mengisi") } })
    fun money(value: Long) = if (hidden && !editing) "********" else AmountFormat.rupiah(value)
    fun begin() {
        val previous = state.latest
        cash = "" // Always confirm actual liquid money; never infer Rp3.2m or forecast income.
        bills = "0"; buffer = "0"
        business = (previous?.business ?: 1_000_000).toString()
        investment = (previous?.investment ?: 1_500_000).toString()
        daily = transport.dailyAmount.toString(); weekdays = transport.weekdaysOnly
        pay25 = previous?.expectedOn25?.takeIf { it > 0 }?.toString() ?: ""
        pay1 = previous?.expectedOn1?.takeIf { it > 0 }?.toString() ?: ""
        payday = PaydayPlan.nextPayday(state.today).toEpochDay()
        editing = true
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Cukup sampai pemasukan berikutnya", style = MaterialTheme.typography.headlineSmall)
            Text("Atur uang yang sudah ada. Perkiraan gaji tidak menambah saldo atau ruang belanja.", style = MaterialTheme.typography.bodyMedium)
        }
        if (!state.loaded) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        else if (!editing) {
            val status = state.status
            if (status == null) item {
                OutlinedCard { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Belum ada rencana aktif")
                    Text("Siapkan saldo tunai, rekening, dan e-wallet yang tersedia. Nilai investasi atau aset yang belum dicairkan tidak dimasukkan.")
                    Button({ begin() }) { Text("Buat rencana") }
                } }
            } else {
                item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${status.plan.start.format(planDateFormat)} – ${status.plan.end.minusDays(1).format(planDateFormat)}", style = MaterialTheme.typography.titleMedium)
                    Text(if (status.expired) "Rencana berakhir · konfirmasi saldo terbaru" else "Sisa belanja hari ini", color = MaterialTheme.colorScheme.primary)
                    Text(if (status.expired) "Perbarui rencana" else money(status.remainingToday.coerceAtLeast(0)), style = MaterialTheme.typography.headlineMedium)
                    Text("Target per hari: ${money(status.dailyBudget)}")
                    Text("Sisa dana bebas periode: ${money(status.freeRemaining)}")
                    if (status.freeRemaining < 0) Text("Dana belum menutup seluruh alokasi. Kurangi target yang bisa ditunda sebelum menambah pengeluaran.", color = MaterialTheme.colorScheme.error)
                    OutlinedButton({ begin() }) { Text("Konfirmasi saldo & perbarui") }
                } } }
                item { OutlinedCard { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alokasi saat rencana dibuat", style = MaterialTheme.typography.titleMedium)
                    Text("Saldo dikonfirmasi: ${money(status.plan.availableCash)}")
                    Text("Tagihan: ${money(status.plan.bills)} · Penyangga: ${money(status.plan.buffer)}")
                    Text("Modal bisnis: ${money(status.plan.business)}")
                    Text("Investasi: ${money(status.plan.investment)}")
                    Text("Transportasi sampai gajian: ${money(status.plan.transportReserve)}")
                    Text("Dana bebas = saldo − seluruh alokasi. Target harian = dana bebas ÷ ${status.plan.days} hari. Sisa hari ini dibatasi dana bebas yang masih ada.")
                    Text("Tagihan terencana dan Transportasi rutin menggunakan cadangannya terlebih dahulu. Kelebihan masuk pemakaian budget bebas. Top-up sendiri tidak dihitung sebagai belanja.")
                    Text("Hari pertama dihitung mulai waktu konfirmasi saldo. Jika saldo dikoreksi atau ada transaksi lama yang baru masuk, konfirmasi saldo lagi.", style = MaterialTheme.typography.bodySmall)
                    Text("Jika dana bisnis atau investasi sudah keluar dari saldo likuid, konfirmasi saldo lagi dan isi hanya bagian alokasi yang masih tersisa.", style = MaterialTheme.typography.bodySmall)
                } } }
                item {
                    val next = PaydayPlan.nextPayday(status.plan.end)
                    Text("Jadwal pemasukan", style = MaterialTheme.typography.titleMedium)
                    Text("${status.plan.end.format(planDateFormat)}: ${money(if (status.plan.end.dayOfMonth == 25) status.plan.expectedOn25 else status.plan.expectedOn1)} (perkiraan)")
                    Text("${next.format(planDateFormat)}: ${money(if (next.dayOfMonth == 25) status.plan.expectedOn25 else status.plan.expectedOn1)} (perkiraan)")
                    Text("Angka 0 berarti belum diisi. Pada tanggal pemasukan, catat uang yang benar-benar diterima dan buat rencana berikutnya. Jadwal dapat digeser bila terlambat.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            item {
                MoneyField("Saldo likuid yang tersedia sekarang", cash, { cash = it })
                TextButton({ cash = state.accountCash.coerceAtLeast(0).toString() }) { Text("Gunakan total estimasi rekening") }
                Text("Periksa angka dengan saldo asli. Sertakan uang yang akan dilindungi di bawah; jangan sertakan nilai investasi yang belum dicairkan.", style = MaterialTheme.typography.bodySmall)
            }
            item {
                val dates = (1L..62L).map { state.today.plusDays(it) }
                ChoiceField("Pemasukan berikutnya", LocalDate.ofEpochDay(payday).format(planDateFormat), dates.map { it.format(planDateFormat) }, { payday = dates[it].toEpochDay() })
            }
            item { MoneyField("Tagihan belum dibayar sebelum gajian", bills, { bills = it }); Text("Pembayarannya pilih kategori Tagihan terencana.", style = MaterialTheme.typography.bodySmall) }
            item { MoneyField("Dana penyangga yang tidak dibelanjakan", buffer, { buffer = it }) }
            item { MoneyField("Lindungi untuk modal bisnis", business, { business = it }) }
            item { MoneyField("Lindungi untuk investasi", investment, { investment = it }); Text("Target bulanan Rp2,5 juta adalah rencana. Sesuaikan bagian yang masih ada dalam saldo ini; tidak dipotong lagi otomatis saat gajian.", style = MaterialTheme.typography.bodySmall) }
            item { MoneyField("Transportasi per hari", daily, { daily = it }); ChoiceField("Hari perjalanan", if (weekdays) "Senin–Jumat" else "Setiap hari", listOf("Senin–Jumat", "Setiap hari"), { weekdays = it == 0 }) }
            item { MoneyField("Perkiraan pemasukan tanggal 25 (opsional)", pay25, { pay25 = it }) }
            item { MoneyField("Perkiraan pemasukan tanggal 1 (opsional)", pay1, { pay1 = it }) }
            item {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                message?.let { Text(it) }
                Button(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                    val values = listOf(cash, bills, buffer, business, investment, daily).map { it.toLongOrNull() }
                    if (values.any { it == null }) error = "Isi semua alokasi dengan angka. Gunakan 0 jika tidak ada."
                    else {
                        val plan = PaydayPlan(System.currentTimeMillis(), state.today.toEpochDay(), payday, values[0]!!,
                            values[1]!!, values[2]!!, values[3]!!, values[4]!!, values[5]!!, weekdays, pay25.toLongOrNull() ?: 0, pay1.toLongOrNull() ?: 0)
                        try { plan.validate(); error = null; vm.save(plan) { editing = false } }
                        catch (e: IllegalArgumentException) { error = e.message }
                    }
                }) { Text(if (saving) "Menyimpan…" else "Konfirmasi saldo & simpan rencana") }
                TextButton({ discard = true }, enabled = !saving) { Text("Batal") }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
