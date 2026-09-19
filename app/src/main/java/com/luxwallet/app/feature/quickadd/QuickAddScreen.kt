@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.quickadd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun QuickAddScreen(onDone: () -> Unit = {}) {
    val viewModel = luxViewModel { QuickAddViewModel.create(it) }
    val options by viewModel.options.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val error by viewModel.error.collectAsState()

    var kind by remember { mutableStateOf(QuickAddKind.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var accountExpanded by remember { mutableStateOf(false) }
    var destAccountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var selectedDestAccountId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var increasesBalance by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Catat transaksi", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        if (options.accounts.isEmpty()) Text("Tambahkan rekening melalui Pengaturan sebelum mencatat transaksi.")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            QuickAddKind.entries.forEachIndexed { index, k ->
                SegmentedButton(
                    selected = kind == k,
                    onClick = { kind = k },
                    shape = SegmentedButtonDefaults.itemShape(index, QuickAddKind.entries.size)
                ) { Text(k.name.replace('_', ' ')) }
            }
        }

        OutlinedTextField(amountText, { amountText = it }, label = { Text("Nominal (Rp)") }, modifier = Modifier.fillMaxWidth())

        AccountDropdown(
            label = if (kind == QuickAddKind.TRANSFER) "Rekening asal" else "Rekening",
            selectedId = selectedAccountId,
            accounts = options.accounts,
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = it },
            onSelect = { selectedAccountId = it }
        )

        if (kind == QuickAddKind.TRANSFER) {
            AccountDropdown(
                label = "Rekening tujuan",
                selectedId = selectedDestAccountId,
                accounts = options.accounts,
                expanded = destAccountExpanded,
                onExpandedChange = { destAccountExpanded = it },
                onSelect = { selectedDestAccountId = it }
            )
        }

        if (kind == QuickAddKind.EXPENSE || kind == QuickAddKind.INCOME) {
            val selectedName = options.categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Pilih kategori"
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                TextField(
                    value = selectedName, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    options.categories.forEach { c ->
                        DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedCategoryId = c.id; categoryExpanded = false })
                    }
                }
            }
            OutlinedTextField(merchant, { merchant = it }, label = { Text("Merchant (opsional)") }, modifier = Modifier.fillMaxWidth())
        }

        if (kind == QuickAddKind.BALANCE_ADJUSTMENT) {
            Text("Menambah saldo")
            Switch(checked = increasesBalance, onCheckedChange = { increasesBalance = it })
        }

        OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())

        error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        Button(enabled = !saving && selectedAccountId != null, onClick = {
            val amount = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(amountText) ?: 0L
            val accountId = selectedAccountId ?: return@Button
            viewModel.submit(
                kind = kind, amount = amount, accountId = accountId, destinationAccountId = selectedDestAccountId,
                categoryId = selectedCategoryId, merchantName = merchant.ifBlank { null }, note = note.ifBlank { null },
                adjustmentIncreasesBalance = increasesBalance, onSaved = onDone
            )
        }) { Text(if (saving) "Menyimpan…" else "Simpan transaksi") }
    }
}

@Composable
private fun AccountDropdown(
    label: String,
    selectedId: Long?,
    accounts: List<com.luxwallet.app.core.database.entity.AccountEntity>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Long) -> Unit
) {
    val selectedName = accounts.firstOrNull { it.id == selectedId }?.name ?: "Pilih rekening"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        TextField(
            value = selectedName, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            accounts.forEach { a ->
                DropdownMenuItem(text = { Text(a.name) }, onClick = { onSelect(a.id); onExpandedChange(false) })
            }
        }
    }
}
