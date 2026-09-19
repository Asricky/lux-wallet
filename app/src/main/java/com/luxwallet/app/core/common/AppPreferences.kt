package com.luxwallet.app.core.common

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luxwallet.app.core.model.RawRetentionPolicy
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lux_wallet_settings")

/**
 * App-level preferences that don't belong in Room (PRD §40 Settings). Financial profile numbers
 * (income/obligations/targets) live in Room's `financial_profile` table instead, since Safe-to-
 * Spend and investment guidance need to query them reactively alongside transactions.
 */
class AppPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val ENABLED_SOURCES = stringSetPreferencesKey("enabled_sources")
        val RAW_RETENTION_POLICY = stringPreferencesKey("raw_retention_policy")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val SCREENSHOT_PROTECTION_ENABLED = booleanPreferencesKey("screenshot_protection_enabled")
        val AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("auto_lock_timeout_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
        val PACKAGE_MAPPINGS = stringSetPreferencesKey("package_mappings")
        val PACKAGE_DISCOVERY_ENABLED = booleanPreferencesKey("package_discovery_enabled")
        val DISCOVERED_PACKAGES = stringSetPreferencesKey("discovered_packages")
    }

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    val enabledSources: Flow<Set<SourceApp>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.ENABLED_SOURCES]
        if (raw == null) {
            SourceApp.entries.toSet() // all supported sources enabled by default
        } else {
            raw.mapNotNull { name -> runCatching { SourceApp.valueOf(name) }.getOrNull() }.toSet()
        }
    }

    suspend fun setSourceEnabled(source: SourceApp, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_SOURCES] ?: SourceApp.entries.map { it.name }.toSet()
            prefs[Keys.ENABLED_SOURCES] = if (enabled) current + source.name else current - source.name
        }
    }

    val rawRetentionPolicy: Flow<RawRetentionPolicy> = context.dataStore.data.map { prefs ->
        prefs[Keys.RAW_RETENTION_POLICY]?.let { runCatching { RawRetentionPolicy.valueOf(it) }.getOrNull() }
            ?: RawRetentionPolicy.DAYS_7 // PRD §36 "Default recommendation: short retention"
    }

    suspend fun setRawRetentionPolicy(policy: RawRetentionPolicy) {
        context.dataStore.edit { it[Keys.RAW_RETENTION_POLICY] = policy.name }
    }

    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_LOCK_ENABLED] ?: false }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_LOCK_ENABLED] = enabled }
    }

    val screenshotProtectionEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SCREENSHOT_PROTECTION_ENABLED] ?: false }

    suspend fun setScreenshotProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCREENSHOT_PROTECTION_ENABLED] = enabled }
    }

    val autoLockTimeoutMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.AUTO_LOCK_TIMEOUT_MINUTES] ?: 1 }

    suspend fun setAutoLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT_MINUTES] = minutes }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    // PRD §39: developer mode must be disabled by default in production builds; debug builds may
    // default it on for convenience, controlled entirely by BuildConfig, never hardcoded true here.
    val developerModeEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.DEVELOPER_MODE_ENABLED] ?: com.luxwallet.app.BuildConfig.DEVELOPER_MODE_DEFAULT
    }

    suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DEVELOPER_MODE_ENABLED] = enabled }
    }

    // --- Package mapping (PRD §39) -------------------------------------------------------------
    // Play Store package names change between releases, which silently breaks capture for a source.
    // These let the user repair that themselves from the Notification Lab instead of waiting for an
    // app update. Stored as "packageName|SOURCE" strings so the set stays a plain DataStore type.

    val packageMappings: Flow<Map<String, SourceApp>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PACKAGE_MAPPINGS].orEmpty().mapNotNull { entry ->
            val pkg = entry.substringBefore('|', "")
            val source = runCatching { SourceApp.valueOf(entry.substringAfter('|', "")) }.getOrNull()
            if (pkg.isBlank() || source == null) null else pkg to source
        }.toMap()
    }

    suspend fun mapPackage(packageName: String, source: SourceApp) {
        context.dataStore.edit { prefs ->
            val kept = prefs[Keys.PACKAGE_MAPPINGS].orEmpty().filterNot { it.substringBefore('|') == packageName }
            prefs[Keys.PACKAGE_MAPPINGS] = kept.toSet() + "$packageName|${source.name}"
        }
    }

    suspend fun unmapPackage(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PACKAGE_MAPPINGS] =
                prefs[Keys.PACKAGE_MAPPINGS].orEmpty().filterNot { it.substringBefore('|') == packageName }.toSet()
        }
    }

    /**
     * Discovery mode records only the *package names* of unrecognised notifications — never their
     * title, text, or any other content — so the user can find which package their bank app
     * actually posts under. Off by default.
     */
    val packageDiscoveryEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.PACKAGE_DISCOVERY_ENABLED] ?: false }

    suspend fun setPackageDiscoveryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PACKAGE_DISCOVERY_ENABLED] = enabled }
    }

    val discoveredPackages: Flow<Set<String>> =
        context.dataStore.data.map { it[Keys.DISCOVERED_PACKAGES].orEmpty() }

    suspend fun recordDiscoveredPackage(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISCOVERED_PACKAGES] = prefs[Keys.DISCOVERED_PACKAGES].orEmpty() + packageName
        }
    }

    suspend fun clearDiscoveredPackages() {
        context.dataStore.edit { it[Keys.DISCOVERED_PACKAGES] = emptySet() }
    }
}
