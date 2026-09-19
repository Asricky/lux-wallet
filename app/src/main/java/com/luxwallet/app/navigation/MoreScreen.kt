package com.luxwallet.app.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement

private data class MoreItem(val label: String, val route: String)

private val MORE_ITEMS = listOf(
    MoreItem("Transaksi", LuxDestinations.TRANSACTIONS),
    MoreItem("Perlu ditinjau", LuxDestinations.NEEDS_REVIEW),
    MoreItem("Anggaran & target", LuxDestinations.BUDGETS_GOALS),
    MoreItem("Wawasan keuangan", LuxDestinations.INSIGHTS),
    MoreItem("Pengaturan", LuxDestinations.SETTINGS)
)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(MORE_ITEMS) { item ->
            Card(Modifier.fillMaxWidth().clickable { onNavigate(item.route) }) {
                Text(item.label, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
