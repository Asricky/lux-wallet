package com.luxwallet.app.feature.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.component.MoneyField
import com.luxwallet.app.engine.FinanceCalculator
import com.luxwallet.app.parser.core.AmountParser

@Composable fun CalculatorScreen() {
    var tab by rememberSaveable { mutableStateOf(0) }
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("0") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var initial by rememberSaveable { mutableStateOf("0") }
    var monthly by rememberSaveable { mutableStateOf("1500000") }
    var rate by rememberSaveable { mutableStateOf("6") }
    var years by rememberSaveable { mutableStateOf("10") }
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val accounts by app.accountRepository.observeActiveAccounts().collectAsState(initial = emptyList())
    val assets by app.assetRepository.observeAll().collectAsState(initial = emptyList())
    val debts by app.liabilityRepository.observeAll().collectAsState(initial = emptyList())
    val total = accounts.filter { it.includeInNetWorth }.sumOf { it.currentEstimatedBalance } +
        assets.filter { it.includeInNetWorth }.sumOf { it.currentValue } - debts.sumOf { it.currentOutstanding }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Kalkulator keuangan", style = MaterialTheme.typography.headlineMedium)
        TabRow(tab) { listOf("Hitung aset", "Pertumbuhan").forEachIndexed { i, title -> Tab(tab == i, { tab = i }, text = { Text(title) }) } }
        if (tab == 0) {
            OutlinedTextField(expression, { expression = it.take(250); error = null }, Modifier.fillMaxWidth(), label = { Text("Perhitungan") })
            Text(result, style = MaterialTheme.typography.headlineMedium)
            TextButton({ expression += total.toString() }) { Text("Masukkan kekayaan bersih saat ini") }
            listOf(listOf("C", "(", ")", "⌫"), listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"),
                listOf("1", "2", "3", "−"), listOf("0", ".", "%", "+")).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { key ->
                    FilledTonalButton(onClick = {
                        error = null
                        when (key) { "C" -> { expression = ""; result = "0" }; "⌫" -> expression = expression.dropLast(1)
                            else -> if (expression.length < 250) expression += key }
                    }, modifier = Modifier.weight(1f).heightIn(min = 52.dp), contentPadding = PaddingValues(4.dp)) { Text(key, style = MaterialTheme.typography.titleLarge) }
                } }
            }
            Button({ try { result = FinanceCalculator.evaluate(expression).toPlainString(); error = null }
                catch (e: Exception) { error = e.message ?: "Periksa perhitungan" } }, Modifier.fillMaxWidth()) { Text("=") }
            Text("Gunakan angka tanpa pemisah ribuan. Persen berarti ÷100. Hasil kalkulator tidak mengubah saldo.", style = MaterialTheme.typography.bodySmall)
        } else {
            MoneyField("Modal awal", initial, { initial = it })
            MoneyField("Setoran tiap akhir bulan", monthly, { monthly = it })
            OutlinedTextField(rate, { rate = it }, Modifier.fillMaxWidth(), label = { Text("Asumsi hasil efektif per tahun (%)") }, singleLine = true)
            OutlinedTextField(years, { years = it }, Modifier.fillMaxWidth(), label = { Text("Durasi (tahun)") }, singleLine = true)
            val projection = runCatching {
                FinanceCalculator.compound(AmountParser.normalizeOrNull(initial)?.toDouble() ?: error("Modal tidak valid"),
                    AmountParser.normalizeOrNull(monthly)?.toDouble() ?: error("Setoran tidak valid"),
                    rate.replace(',', '.').toDouble(), years.toInt())
            }.getOrNull()
            if (projection != null) Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Simulasi nilai akhir", style = MaterialTheme.typography.titleMedium)
                    Text(AmountFormat.rupiah(projection.futureValue.toLong()), style = MaterialTheme.typography.headlineMedium)
                    Text("Total setoran: ${AmountFormat.rupiah(projection.principal.toLong())}")
                    Text("Pertumbuhan: ${AmountFormat.rupiah((projection.futureValue - projection.principal).toLong())}")
                }
            } else Text("Isi nilai yang valid: durasi 1–60 tahun dan hasil −99% hingga 100%.")
            Text("Simulasi, bukan janji hasil. Angka 6% hanya contoh; pajak, biaya, dan inflasi belum dimasukkan. Hasil investasi dapat turun. Perhitungan mengasumsikan hasil diinvestasikan kembali.", style = MaterialTheme.typography.bodySmall)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
