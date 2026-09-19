@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.luxwallet.app.feature.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.luxwallet.app.core.model.RawRetentionPolicy
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.ThemeMode
import com.luxwallet.app.core.ui.luxViewModel
import java.io.File

@Composable
fun SettingsScreen(
    onOpenAccounts: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onOpenRules: () -> Unit = {},
    onOpenNotificationLab: () -> Unit = {}
) {
    val viewModel = luxViewModel { SettingsViewModel.create(it) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val temp = File(context.cacheDir, "lux_export_${System.currentTimeMillis()}.bin")
        viewModel.exportBackup(temp) { success, error ->
            if (success) {
                context.contentResolver.openOutputStream(uri)?.use { out -> temp.inputStream().use { it.copyTo(out) } }
                Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Export failed: $error", Toast.LENGTH_LONG).show()
            }
            temp.delete()
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val temp = File(context.cacheDir, "lux_transactions_${System.currentTimeMillis()}.csv")
        viewModel.exportCsv(temp) { success, error ->
            if (success) {
                context.contentResolver.openOutputStream(uri)?.use { out -> temp.inputStream().use { it.copyTo(out) } }
                Toast.makeText(context, "CSV exported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "CSV export failed: $error", Toast.LENGTH_LONG).show()
            }
            temp.delete()
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val temp = File(context.cacheDir, "lux_restore_${System.currentTimeMillis()}.bin")
        context.contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { input.copyTo(it) } }
        viewModel.restoreBackup(temp) { result ->
            val message = when (result) {
                is com.luxwallet.app.data.RestoreResult.Success -> "Restore complete"
                is com.luxwallet.app.data.RestoreResult.IncompatibleSchema -> "Backup schema v${result.backupVersion} is incompatible with app schema v${result.currentVersion}"
                is com.luxwallet.app.data.RestoreResult.Failure -> "Restore failed: ${result.message}"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            temp.delete()
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Pengaturan", style = MaterialTheme.typography.headlineMedium) }
        item { com.luxwallet.app.core.ui.component.NotificationStatusCard() }
        item {
            SettingsSection("Rekening") {
                SettingsLinkRow("Kelola rekening", onOpenAccounts)
            }
        }

        item {
            SettingsSection("Sumber notifikasi") {
                SourceApp.entries.forEach { source ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(source.name)
                        Switch(
                            checked = source in state.enabledSources,
                            onCheckedChange = { viewModel.setSourceEnabled(source, it) }
                        )
                    }
                }
            }
        }

        item {
            SettingsSection("Kategori & aturan") {
                SettingsLinkRow("Kategori", onOpenCategories)
                SettingsLinkRow("Aturan merchant", onOpenRules)
            }
        }

        item {
            SettingsSection("Keamanan") {
                ToggleRow("Kunci biometrik", state.biometricLockEnabled) { enabled ->
                    val activity = context as? androidx.fragment.app.FragmentActivity
                    if (!enabled || (activity != null && com.luxwallet.app.core.security.BiometricAuthManager.availability(activity) ==
                        com.luxwallet.app.core.security.BiometricAvailability.AVAILABLE)) viewModel.setBiometricLockEnabled(enabled)
                    else Toast.makeText(context, "Atur PIN atau biometrik perangkat terlebih dahulu.", Toast.LENGTH_LONG).show()
                }
                ToggleRow("Blokir tangkapan layar", state.screenshotProtectionEnabled, viewModel::setScreenshotProtectionEnabled)
                RetentionDropdown(state.rawRetentionPolicy, viewModel::setRawRetentionPolicy)
            }
        }

        item {
            SettingsSection("Tampilan") {
                ThemeDropdown(state.themeMode, viewModel::setThemeMode)
            }
        }

        item {
            SettingsSection("Cadangan data") {
                SettingsLinkRow("Ekspor cadangan terenkripsi") { exportLauncher.launch("lux-wallet-backup.bin") }
                SettingsLinkRow("Pulihkan cadangan") { restoreLauncher.launch(arrayOf("*/*")) }
                SettingsLinkRow("Ekspor transaksi (CSV)") { csvExportLauncher.launch("lux-wallet-transactions.csv") }
            }
        }

        item {
            SettingsSection("Diagnostik") {
                ToggleRow("Mode diagnostik", state.developerModeEnabled, viewModel::setDeveloperModeEnabled)
                if (state.developerModeEnabled) {
                    SettingsLinkRow("Notification Lab", onOpenNotificationLab)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelMedium)
            content()
        }
    }
}

@Composable
private fun SettingsLinkRow(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun RetentionDropdown(current: RawRetentionPolicy, onSelect: (RawRetentionPolicy) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            value = current.name, onValueChange = {}, readOnly = true, label = { Text("Simpan notifikasi mentah") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RawRetentionPolicy.entries.forEach { policy ->
                DropdownMenuItem(text = { Text(policy.name) }, onClick = { onSelect(policy); expanded = false })
            }
        }
    }
}

@Composable
private fun ThemeDropdown(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            value = current.name, onValueChange = {}, readOnly = true, label = { Text("Tema") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.entries.forEach { mode ->
                DropdownMenuItem(text = { Text(mode.name) }, onClick = { onSelect(mode); expanded = false })
            }
        }
    }
}
