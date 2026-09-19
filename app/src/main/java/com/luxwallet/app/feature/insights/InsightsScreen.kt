package com.luxwallet.app.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.ui.luxViewModel
import com.luxwallet.app.core.ui.theme.LocalLuxSemanticColors
import com.luxwallet.app.engine.InsightSeverity

@Composable
fun InsightsScreen() {
    val viewModel = luxViewModel { InsightsViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    if (state.insights.isEmpty() && !state.isLoading) {
        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No notable changes yet. Insights appear once you have a few months of activity.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.insights.isNotEmpty()) {
            item { Text("INSIGHTS", style = MaterialTheme.typography.labelMedium) }
        }
        items(state.insights) { insight ->
            val semantic = LocalLuxSemanticColors.current
            val color = when (insight.severity) {
                InsightSeverity.WARNING -> semantic.warning
                InsightSeverity.POSITIVE -> semantic.positive
                InsightSeverity.INFO -> semantic.neutral
            }
            Card(Modifier.fillMaxWidth()) {
                Text(insight.message, Modifier.padding(16.dp), color = color, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (state.guidanceItems.isNotEmpty()) {
            item { Text("INVESTMENT GUIDANCE", style = MaterialTheme.typography.labelMedium) }
            item {
                Text(
                    state.guidancePriority,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(state.guidanceItems) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.category, style = MaterialTheme.typography.titleMedium)
                        Text("Why this guidance?", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        item.rationale.forEach { line ->
                            Text("• $line", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
