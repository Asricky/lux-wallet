@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.accounts

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.common.AmountFormat
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.core.ui.luxViewModel

@Composable
fun AccountsScreen() {
    val viewModel = luxViewModel { AccountsViewModel.create(it) }
    val saving by viewModel.saving.collectAsState()
    val error by viewModel.error.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var name by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var provider by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(AccountProvider.BCA) }
    var openingBalance by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(accounts) { account ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(account.name, fontWeight = FontWeight.Medium)
                        Text(AmountFormat.rupiah(account.currentEstimatedBalance), style = MaterialTheme.typography.titleMedium)
                    }
                    Text("${account.kind.name} • ${account.provider.name}", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = account.includeInNetWorth, onCheckedChange = { viewModel.setIncludeInNetWorth(account, it) })
                        Text("Hitung dalam kekayaan bersih", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tambah rekening", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(name, { name = it }, label = { Text("Nama rekening") }, modifier = Modifier.fillMaxWidth())

                    com.luxwallet.app.core.ui.component.ChoiceField("Penyedia", provider.name,
                        AccountProvider.entries.map { it.name }, { provider = AccountProvider.entries[it] })

                    com.luxwallet.app.core.ui.component.MoneyField("Saldo saat ini", openingBalance, { openingBalance = it })

                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(enabled = !saving, onClick = {
                        val balance = com.luxwallet.app.parser.core.AmountParser.normalizeOrNull(openingBalance)
                        if (balance == null || balance < 0 || name.isBlank()) { viewModel.error.value = "Isi nama dan saldo yang valid."; return@Button }
                        if (name.isNotBlank()) {
                            val kind = when (provider) {
                                AccountProvider.BCA, AccountProvider.SEABANK -> AccountKind.BANK
                                AccountProvider.GOPAY, AccountProvider.SHOPEEPAY -> AccountKind.EWALLET
                                AccountProvider.CASH, AccountProvider.OTHER -> AccountKind.MANUAL
                            }
                            viewModel.createAccount(name, kind, provider, balance) {
                                name = ""
                                openingBalance = ""
                            }
                        }
                    }) { Text(if (saving) "Menyimpan…" else "Simpan rekening") }
                }
            }
        }
    }
}
