@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun TransactionDetailScreen(transactionId: Long) {
    val viewModel = luxViewModel { TransactionDetailViewModel.create(it, transactionId) }
    val state by viewModel.uiState.collectAsState()
    val tx = state.transaction ?: run {
        Text("Loading…", Modifier.padding(16.dp))
        return
    }

    var merchantText by remember(tx.id) { mutableStateOf(tx.merchantName.orEmpty()) }
    var noteText by remember(tx.id) { mutableStateOf(tx.note.orEmpty()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var learnRule by remember { mutableStateOf(true) }
    val selectedCategoryName = state.mainCategories.firstOrNull { it.id == tx.categoryId }?.name ?: "Uncategorized"

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(AmountFormat.rupiah(tx.amount), style = MaterialTheme.typography.headlineMedium)
        Text("${tx.type.name.replace('_', ' ')} • ${tx.reviewStatus.name.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium)

        if (tx.reviewStatus == ReviewStatus.NEEDS_REVIEW && tx.reviewReason != null) {
            Text("Needs review: ${tx.reviewReason.name.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = merchantText,
            onValueChange = { merchantText = it },
            label = { Text("Merchant / Counterparty") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
            TextField(
                value = selectedCategoryName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                state.mainCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.updateCategory(category.id, learnRule)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        if (!merchantText.isBlank()) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = learnRule, onCheckedChange = { learnRule = it })
                Text("Remember this category for \"$merchantText\"", style = MaterialTheme.typography.bodyMedium)
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = tx.isExcludedFromCashflow, onCheckedChange = { viewModel.setExcludedFromCashflow(it) })
            Text("Exclude from cashflow & budgets", style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.saveAndConfirm(merchantText, noteText)
            }) { Text("Save & Confirm") }

            OutlinedButton(onClick = { viewModel.markIgnored() }) { Text("Mark as Duplicate / Ignore") }
        }
    }
}
