@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun RulesScreen() {
    val viewModel = luxViewModel { RulesViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()

    var merchantText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.rules) { rule ->
            val categoryName = state.categories.firstOrNull { it.id == rule.categoryId }?.name ?: "Unknown"
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("\"${rule.merchantContains}\" → $categoryName", style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { viewModel.deleteRule(rule) }) { Text("Remove") }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("New merchant rule", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(merchantText, { merchantText = it }, label = { Text("Merchant name contains") }, modifier = Modifier.fillMaxWidth())

                    val selectedName = state.categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Pilih kategori"
                    com.luxwallet.app.core.ui.component.ChoiceField("Kategori", selectedName,
                        state.categories.map { it.name }, { selectedCategoryId = state.categories[it].id })

                    Button(onClick = {
                        selectedCategoryId?.let { viewModel.addRule(merchantText, it) }
                        merchantText = ""
                    }) { Text("Add Rule") }
                }
            }
        }
    }
}
