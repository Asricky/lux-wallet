package com.luxwallet.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luxwallet.app.LuxWalletApp
import com.luxwallet.app.core.common.AppPreferences
import com.luxwallet.app.core.model.RawRetentionPolicy
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.ThemeMode
import com.luxwallet.app.data.BackupRepository
import com.luxwallet.app.data.CsvExportRepository
import com.luxwallet.app.data.RestoreResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val enabledSources: Set<SourceApp> = emptySet(),
    val biometricLockEnabled: Boolean = false,
    val screenshotProtectionEnabled: Boolean = false,
    val rawRetentionPolicy: RawRetentionPolicy = RawRetentionPolicy.DAYS_7,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val developerModeEnabled: Boolean = false,
    val autoLockTimeoutMinutes: Int = 1
)

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val backupRepository: BackupRepository,
    private val csvExportRepository: CsvExportRepository
) : ViewModel() {

    private val firstFive = combine(
        preferences.enabledSources, preferences.biometricLockEnabled, preferences.screenshotProtectionEnabled,
        preferences.rawRetentionPolicy, preferences.themeMode
    ) { sources, biometric, screenshot, retention, theme ->
        SettingsUiState(
            enabledSources = sources,
            biometricLockEnabled = biometric,
            screenshotProtectionEnabled = screenshot,
            rawRetentionPolicy = retention,
            themeMode = theme
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(firstFive, preferences.developerModeEnabled) { partial, devMode ->
        partial.copy(developerModeEnabled = devMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setSourceEnabled(source: SourceApp, enabled: Boolean) {
        viewModelScope.launch { preferences.setSourceEnabled(source, enabled) }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setBiometricLockEnabled(enabled) }
    }

    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setScreenshotProtectionEnabled(enabled) }
    }

    fun setRawRetentionPolicy(policy: RawRetentionPolicy) {
        viewModelScope.launch { preferences.setRawRetentionPolicy(policy) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setDeveloperModeEnabled(enabled) }
    }

    fun exportBackup(destination: File, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                backupRepository.export(destination)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun restoreBackup(source: File, onResult: (RestoreResult) -> Unit) {
        viewModelScope.launch {
            onResult(backupRepository.restore(source))
        }
    }

    fun exportCsv(destination: File, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                csvExportRepository.exportTransactions(destination)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    companion object {
        fun create(app: LuxWalletApp) = SettingsViewModel(app.preferences, app.backupRepository, app.csvExportRepository)
    }
}
