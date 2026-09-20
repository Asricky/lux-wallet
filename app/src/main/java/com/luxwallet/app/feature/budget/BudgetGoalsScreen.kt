package com.luxwallet.app.feature.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.common.TransportPlan
import com.luxwallet.app.core.database.entity.GoalEntity
import com.luxwallet.app.core.ui.component.*
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.parser.core.AmountParser
import java.time.LocalDate

@Composable fun BudgetGoalsScreen() {
    val vm = luxViewModel { BudgetGoalsViewModel.create(it) }
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()
    val message by vm.message.collectAsState()
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val transport by app.preferences.transportPlan.collectAsState(initial = null)
    if (state.isLoading || transport == null) { CircularProgressIndicator(Modifier.padding(32.dp)); return }
    var income by rememberSaveable { mutableStateOf(state.profile.expectedMonthlyIncome.toString()) }
    var obligations by rememberSaveable { mutableStateOf(state.profile.fixedObligations.toString()) }
    var debt by rememberSaveable { mutableStateOf(state.profile.debtPayments.toString()) }
    var savings by rememberSaveable { mutableStateOf(state.profile.savingsTargetMonthly.toString()) }
    var investment by rememberSaveable { mutableStateOf(state.profile.investmentTargetMonthly.toString()) }
    var planned by rememberSaveable { mutableStateOf(state.profile.plannedExpensesMonthly.toString()) }
    var buffer by rememberSaveable { mutableStateOf(state.profile.safetyBufferMonthly.toString()) }
    var daily by rememberSaveable { mutableStateOf(transport!!.dailyAmount.toString()) }
    var weekdays by rememberSaveable { mutableStateOf(transport!!.weekdaysOnly) }
    var goalName by rememberSaveable { mutableStateOf("") }
    var goalAmount by rememberSaveable { mutableStateOf("") }
    val uri = LocalUriHandler.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.imePadding()) {
        item { Text("Rencana keuangan", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Pendapatan & kewajiban", style = MaterialTheme.typography.titleMedium)
                MoneyField("Pendapatan per bulan", income, { income = it })
                MoneyField("Kewajiban tetap selain transportasi", obligations, { obligations = it })
                MoneyField("Cicilan utang", debt, { debt = it })
                MoneyField("Pengeluaran terencana lain", planned, { planned = it })
                MoneyField("Dana penyangga", buffer, { buffer = it })
            } }
        }
        item {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Transportasi rutin", style = MaterialTheme.typography.titleMedium)
                MoneyField("Biaya per hari perjalanan", daily, { daily = it })
                ChoiceField("Jadwal perjalanan", if (weekdays) "Hari kerja · Senin–Jumat" else "Setiap hari",
                    listOf("Hari kerja · Senin–Jumat", "Setiap hari"), { weekdays = it == 0 })
                val plan = TransportPlan(AmountParser.normalizeOrNull(daily)?.coerceAtLeast(0) ?: 0, weekdays)
                Text("Cadangan bulan ini: ${com.luxwallet.app.core.common.privateRupiah(plan.monthlyReserve(LocalDate.now()))} untuk ${plan.daysInMonth(LocalDate.now())} hari.", style = MaterialTheme.typography.bodyMedium)
                Text("Top-up kapan saja melalui Pindah saldo / top-up sendiri. Catat pemakaiannya pada kategori Transportasi rutin. Dana ini dicadangkan sekali, tidak masuk penggunaan uang belanja bebas.", style = MaterialTheme.typography.bodySmall)
            } }
        }
        item {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Sisihkan sebelum belanja", style = MaterialTheme.typography.titleMedium)
                MoneyField("Tabungan modal bisnis / bulan", savings, { savings = it })
                MoneyField("Investasi jangka panjang / bulan", investment, { investment = it })
                TextButton({ savings = "1000000"; investment = "1500000" }) { Text("Gunakan target total Rp2.500.000") }
                Text("Target mengurangi alokasi belanja, bukan otomatis memindahkan uang. Catat pemindahan ke rekening tabungan sendiri sebagai transfer.", style = MaterialTheme.typography.bodySmall)
            } }
        }
        item {
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Button(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                val values = listOf(income, obligations, debt, savings, investment, planned, buffer, daily).map { AmountParser.normalizeOrNull(it) }
                if (values.any { it == null || it < 0 }) vm.message.value = "Semua nominal harus valid, nol atau positif."
                else vm.savePlan(state.profile.copy(expectedMonthlyIncome = values[0]!!, fixedObligations = values[1]!!,
                    debtPayments = values[2]!!, savingsTargetMonthly = values[3]!!, investmentTargetMonthly = values[4]!!,
                    plannedExpensesMonthly = values[5]!!, safetyBufferMonthly = values[6]!!), values[7]!!, weekdays)
            }) { Text(if (saving) "Menyimpan…" else "Simpan rencana") }
        }
        item {
            OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pilihan untuk pertumbuhan dana", style = MaterialTheme.typography.titleMedium)
                Text("Modal bisnis: simpan terpisah di tabungan yang mudah dicairkan. Dana darurat dan kebutuhan dekat sebaiknya tetap likuid.")
                Text("Rp1,5 juta investasi: jika dana darurat belum cukup atau horizon belum jelas, pertimbangkan tabungan/deposito sesuai jatuh tempo atau reksa dana pasar uang. Reksa dana dapat turun dan tidak dijamin LPS.")
                Text("Untuk tujuan lebih dari 5 tahun dan siap menghadapi penurunan nilai, pertimbangkan reksa dana indeks terdiversifikasi dengan biaya rendah. SBN ritel dapat dipertimbangkan bila jangka waktunya cocok.")
                Text("Pertumbuhan majemuk berasal dari hasil yang diinvestasikan kembali. Tidak ada produk dengan hasil terbaik yang pasti; bandingkan biaya, risiko, likuiditas, dan izin penjual.")
                TextButton({ uri.openUri("https://www.ojk.go.id/Files/box/BukuSakuOJK.pdf") }) { Text("Pelajari risiko reksa dana · OJK ↗") }
                TextButton({ uri.openUri("https://www.kemenkeu.go.id/sukukritel") }) { Text("Pelajari SBN ritel · Kemenkeu ↗") }
            } }
        }
        item { Text("Target pribadi", style = MaterialTheme.typography.titleLarge) }
        items(state.goals, key = { it.id }) { goal ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth())
                Text("${com.luxwallet.app.core.common.privateRupiah(goal.currentAmount)} / ${com.luxwallet.app.core.common.privateRupiah(goal.targetAmount)}")
            } }
        }
        item {
            OutlinedTextField(goalName, { goalName = it }, Modifier.fillMaxWidth(), label = { Text("Nama target") })
            MoneyField("Nominal target", goalAmount, { goalAmount = it })
            TextButton({
                val value = AmountParser.normalizeOrNull(goalAmount)
                if (goalName.isNotBlank() && value != null && value > 0) {
                    vm.addGoal(GoalEntity(name = goalName, targetAmount = value, createdAt = System.currentTimeMillis()))
                    goalName = ""; goalAmount = ""
                }
            }) { Text("Tambah target") }
        }
    }
}
