package com.luxwallet.app.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.ui.component.TransactionCard
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun TransactionsScreen(onTransactionClick: (Long) -> Unit = {}) {
    val viewModel = luxViewModel { TransactionsViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Riwayat transaksi", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 18.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            label = { Text("Cari merchant atau kategori") },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.transactions.isEmpty() && !state.isLoading) {
            Text(
                "Belum ada transaksi. Catat manual atau aktifkan pemantauan notifikasi.",
                Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.transactions, key = { it.id }) { tx ->
                TransactionCard(
                    transaction = tx,
                    sourceAccountName = tx.sourceAccountId?.let { state.accountNames[it] },
                    destinationAccountName = tx.destinationAccountId?.let { state.accountNames[it] },
                    categoryName = tx.categoryId?.let { state.categoryNames[it] },
                    onClick = { onTransactionClick(tx.id) }
                )
            }
        }
    }
}
