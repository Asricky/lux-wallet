@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.cashflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.component.DonutChart
import com.luxwallet.app.core.ui.component.DonutSlice
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun CashflowScreen() {
    val viewModel = luxViewModel { CashflowViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            val months = (0L..59L).map { java.time.YearMonth.now().minusMonths(it) }
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("id", "ID"))
            com.luxwallet.app.core.ui.component.ChoiceField("Periode", state.yearMonth.format(formatter),
                months.map { it.format(formatter) }, { viewModel.setMonth(months[it]) })
        }

        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.tab == CashflowTab.EXPENSE,
                    onClick = { viewModel.setTab(CashflowTab.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Pengeluaran") }
                SegmentedButton(
                    selected = state.tab == CashflowTab.INCOME,
                    onClick = { viewModel.setTab(CashflowTab.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Pemasukan") }
            }
        }

        item { AccountFilterDropdown(state, viewModel) }

        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                DonutChart(
                    slices = state.categories.map { DonutSlice(it.name, it.amount.toDouble(), it.color) },
                    centerLabel = AmountFormat.rupiah(state.total),
                    centerSubLabel = if (state.tab == CashflowTab.EXPENSE) "Total Pengeluaran" else "Total Pemasukan"
                )
            }
        }

        items(state.categories) { slice ->
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(slice.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${slice.percent}%", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(AmountFormat.rupiah(slice.amount), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (state.categories.isEmpty() && !state.isLoading) {
            item { Text("No transactions this month yet.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun AccountFilterDropdown(state: CashflowUiState, viewModel: CashflowViewModel) {
    val selected = state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name ?: "Semua rekening"
    com.luxwallet.app.core.ui.component.ChoiceField("Rekening", selected, listOf("Semua rekening") + state.accounts.map { it.name },
        { viewModel.setAccountFilter(if (it == 0) null else state.accounts[it - 1].id) })
}
