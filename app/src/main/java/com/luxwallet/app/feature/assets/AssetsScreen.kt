package com.luxwallet.app.feature.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.core.ui.theme.LocalLuxSemanticColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

@Composable
fun AssetsScreen() {
    val viewModel = luxViewModel { AssetsViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Kekayaan bersih", style = MaterialTheme.typography.labelLarge)
            Text(AmountFormat.rupiah(state.netWorth), style = MaterialTheme.typography.displaySmall)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Assets ${AmountFormat.rupiah(state.totalAssets)}", style = MaterialTheme.typography.bodyMedium)
                Text("Liabilities ${AmountFormat.rupiah(state.totalLiabilities)}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { SectionHeader("Aset likuid") }
        items(state.liquidAccounts) { account ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(account.name, fontWeight = FontWeight.Medium)
                    Text(AmountFormat.rupiah(account.currentEstimatedBalance), style = MaterialTheme.typography.titleMedium)
                    val reconciled = account.lastReconciledAt?.let {
                        "Last reconciled: ${Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DATE_FORMAT)}"
                    } ?: "Updated from transactions"
                    Text(reconciled, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (state.investments.isNotEmpty()) {
            item { SectionHeader("Investments") }
            items(state.investments) { asset ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(asset.name, fontWeight = FontWeight.Medium)
                        Text(asset.assetClass.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                        Text(AmountFormat.rupiah(asset.currentValue), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        if (state.otherAssets.isNotEmpty()) {
            item { SectionHeader("Other Assets") }
            items(state.otherAssets) { asset ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(asset.name, fontWeight = FontWeight.Medium)
                        Text(asset.assetClass.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                        Text(AmountFormat.rupiah(asset.currentValue), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        if (state.liabilities.isNotEmpty()) {
            item { SectionHeader("Kewajiban") }
            items(state.liabilities) { liability ->
                val semantic = LocalLuxSemanticColors.current
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(liability.name, fontWeight = FontWeight.Medium)
                        Text(liability.type.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                        Text(AmountFormat.rupiah(liability.currentOutstanding), color = semantic.expense, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium)
}
