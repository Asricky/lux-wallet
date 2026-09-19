package com.luxwallet.app.feature.categories

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun CategoriesScreen() {
    val viewModel = luxViewModel { CategoriesViewModel.create(it) }
    val categories by viewModel.categories.collectAsState()
    var newName by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            Card(Modifier.fillMaxWidth()) {
                Text(category.name, Modifier.padding(12.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(newName, { newName = it }, label = { Text("New category") }, modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.addCategory(newName); newName = "" }, modifier = Modifier.padding(start = 8.dp)) {
                    Text("Tambah")
                }
            }
        }
    }
}
