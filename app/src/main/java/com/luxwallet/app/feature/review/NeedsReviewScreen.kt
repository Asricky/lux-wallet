package com.luxwallet.app.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.ui.component.TransactionCard
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun NeedsReviewScreen(onTransactionClick: (Long) -> Unit = {}) {
    val viewModel = luxViewModel { NeedsReviewViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.luxwallet.app.LuxWalletApp
    val observations by app.notificationRepository.observeAll().collectAsState(initial = emptyList())
    val failed = observations.filter { it.parseStatus == com.luxwallet.app.core.model.ParseStatus.FAILED }

    if (state.transactions.isEmpty() && failed.isEmpty() && !state.isLoading) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Semua sudah ditinjau.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Perlu ditinjau", style = MaterialTheme.typography.headlineMedium) }
        items(failed, key = { "observation-${it.id}" }) { observation ->
            androidx.compose.material3.OutlinedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("${observation.sourceApp} • Belum dikenali", style = MaterialTheme.typography.titleSmall)
                    Text(observation.title)
                    Text(observation.bigText ?: observation.text, style = MaterialTheme.typography.bodyMedium)
                    Text("Belum masuk saldo. Catat manual bila ini transaksi yang valid.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
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
