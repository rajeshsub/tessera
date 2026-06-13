package io.github.rajeshsub.tessera.data.vault

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PrefsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : PrefsRepository {
    override val clipboardTimeoutSeconds: Flow<Int> =
        dataStore.data.map {
            it[Keys.CLIPBOARD_TIMEOUT] ?: 30
        }

    override val sntpEnabled: Flow<Boolean> =
        dataStore.data.map {
            it[Keys.SNTP_ENABLED] ?: false
        }

    override val theme: Flow<String> =
        dataStore.data.map {
            it[Keys.THEME] ?: "system"
        }

    override val biometricEnabled: Flow<Boolean> =
        dataStore.data.map {
            it[Keys.BIOMETRIC_ENABLED] ?: false
        }

    override val lastBackupTimestamp: Flow<Long> =
        dataStore.data.map {
            it[Keys.LAST_BACKUP_TIMESTAMP] ?: 0L
        }

    override suspend fun setClipboardTimeout(seconds: Int) {
        dataStore.edit { it[Keys.CLIPBOARD_TIMEOUT] = seconds }
    }

    override suspend fun setSntpEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SNTP_ENABLED] = enabled }
    }

    override suspend fun setTheme(theme: String) {
        dataStore.edit { it[Keys.THEME] = theme }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setLastBackupTimestamp(millis: Long) {
        dataStore.edit { it[Keys.LAST_BACKUP_TIMESTAMP] = millis }
    }

    internal object Keys {
        val CLIPBOARD_TIMEOUT = intPreferencesKey("clipboard_timeout")
        val SNTP_ENABLED = booleanPreferencesKey("sntp_enabled")
        val THEME = stringPreferencesKey("theme")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }
}
