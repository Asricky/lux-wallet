package com.luxwallet.app.feature.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.common.CashflowMath
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.ui.component.ChoiceField
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.engine.*
import com.luxwallet.app.feature.planner.PlannerViewModel
import com.luxwallet.app.feature.planner.planDateFormat
import com.luxwallet.app.navigation.LuxDestinations
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun CalendarScreen(onNavigate: (String) -> Unit) {
    val vm = luxViewModel { PlannerViewModel(it) }
    val state by vm.state.collectAsState()
    val app = LocalContext.current.applicationContext as LuxWalletApp
    val hidden by app.preferences.amountsHidden.collectAsState(initial = true)
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    val month = YearMonth.parse(monthText)
    val selected = LocalDate.ofEpochDay(selectedDay)
    val locale = Locale("id", "ID")
    val zone = ZoneId.systemDefault()
    val eligible = remember(state.transactions) { CashflowMath.cashflowEligible(state.transactions) }
    val grouped = remember(eligible) { eligible.groupBy { Instant.ofEpochMilli(it.transactionTime).atZone(zone).toLocalDate() } }
    fun money(value: Long) = if (hidden) "Rp ••••••" else AmountFormat.rupiah(value)
    fun changeMonth(value: YearMonth) { monthText = value.toString(); selectedDay = value.atDay(1).toEpochDay() }
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Kalender keuangan", style = MaterialTheme.typography.headlineSmall) }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton({ changeMonth(month.minusMonths(1)) }) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Bulan sebelumnya") }
            Box(Modifier.weight(1f)) {
                val months = (-12L..60L).map { YearMonth.now().minusMonths(it) }
                val format = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
                ChoiceField("Periode", month.format(format), months.map { it.format(format) }, { changeMonth(months[it]) })
            }
            IconButton({ changeMonth(month.plusMonths(1)) }) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Bulan berikutnya") }
        } }
        item {
            val monthly = grouped.filterKeys { YearMonth.from(it) == month }.values.flatten()
            OutlinedCard { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Masuk ${money(CashflowMath.totalIncome(monthly))}")
                Text("Keluar ${money(CashflowMath.totalExpense(monthly))}")
                Text("Selisih ${money(CashflowMath.totalIncome(monthly) - CashflowMath.totalExpense(monthly))}", style = MaterialTheme.typography.titleMedium)
            } }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row { listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
                val offset = month.atDay(1).dayOfWeek.value - 1
                val slots = ((offset + month.lengthOfMonth() + 6) / 7) * 7
                (0 until slots step 7).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        (row until row + 7).forEach { slot ->
                            val day = slot - offset + 1
                            if (day !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).heightIn(min = 56.dp))
                            else {
                                val date = month.atDay(day)
                                val plan = PaydayPlan.forDay(state.plans, date)
                                val status = plan?.let { PaydayMath.status(it, state.transactions, state.transportIds, state.billIds, date, zone) }
                                val marker = when {
                                    date > state.today -> "·"
                                    status == null -> "—"
                                    status.dailyBudget - status.spentToday < 0 -> "−"
                                    else -> "+"
                                }
                                val color = when {
                                    date == selected -> MaterialTheme.colorScheme.primaryContainer
                                    !hidden && marker == "−" -> MaterialTheme.colorScheme.errorContainer
                                    !hidden && marker == "+" -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                Surface(onClick = { selectedDay = date.toEpochDay() }, shape = RoundedCornerShape(10.dp), color = color,
                                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { contentDescription = date.format(planDateFormat) }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(day.toString(), style = MaterialTheme.typography.bodyMedium)
                                        Text(if (hidden) "·" else marker, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Text("+ Dalam budget   − Melebihi budget   — Belum ada rencana", style = MaterialTheme.typography.bodySmall) }
        item {
            val txs = grouped[selected].orEmpty()
            val plan = PaydayPlan.forDay(state.plans, selected)
            val status = plan?.let { PaydayMath.status(it, state.transactions, state.transportIds, state.billIds, selected, zone) }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(selected.format(planDateFormat), style = MaterialTheme.typography.titleLarge)
                Text("Pemasukan: ${money(CashflowMath.totalIncome(txs))}")
                Text("Pengeluaran: ${money(CashflowMath.totalExpense(txs))}")
                Text("Selisih arus kas: ${money(CashflowMath.totalIncome(txs) - CashflowMath.totalExpense(txs))}")
                HorizontalDivider()
                when {
                    selected > state.today -> Text("Hari mendatang · belum ada hasil aktual.")
                    status == null -> Text("Belum ada target budget tersimpan untuk hari ini. Selisih arus kas bukan surplus budget.")
                    else -> {
                        val delta = status.dailyBudget - status.spentToday
                        Text("Budget bebas: ${money(status.dailyBudget)}")
                        Text("Pemakaian budget: ${money(status.spentToday)}")
                        Text("${if (delta < 0) "Minus budget" else if (selected == state.today) "Sisa budget sementara" else "Surplus budget"}: ${money(delta)}", style = MaterialTheme.typography.titleMedium)
                        if (selected == plan.start) Text("Hari pertama: budget dan pemakaian sejak saldo dikonfirmasi. Arus kas di atas tetap mencakup satu hari penuh.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Transportasi & tagihan memakai cadangan masing-masing. Transfer sendiri dan koreksi saldo tidak masuk arus kas. Hari ini belum merupakan hasil akhir.", style = MaterialTheme.typography.bodySmall)
                TextButton({ onNavigate(LuxDestinations.PLANNER) }) { Text("Atur rencana sampai gajian") }
                TextButton({ onNavigate(LuxDestinations.CASHFLOW) }) { Text("Lihat rincian arus kas") }
            } }
        }
        items(grouped[selected].orEmpty().sortedByDescending { it.transactionTime }, key = { it.id }) { tx ->
            OutlinedCard(onClick = { onNavigate(LuxDestinations.transactionDetail(tx.id)) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) {
                    Text(tx.merchantName ?: tx.counterpartyName ?: "Transaksi", Modifier.weight(1f))
                    Text("${if (tx.direction == TransactionDirection.IN) "+" else "−"}${money(tx.amount)}")
                }
            }
        }
    }
}
