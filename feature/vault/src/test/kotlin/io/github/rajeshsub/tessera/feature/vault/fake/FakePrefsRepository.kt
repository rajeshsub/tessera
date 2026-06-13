package io.github.rajeshsub.tessera.feature.vault.fake

import io.github.rajeshsub.tessera.data.vault.PrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePrefsRepository : PrefsRepository {
    private val _clipboardTimeoutSeconds = MutableStateFlow(30)
    private val _sntpEnabled = MutableStateFlow(false)
    private val _theme = MutableStateFlow("system")
    private val _biometricEnabled = MutableStateFlow(false)
    private val _lastBackupTimestamp = MutableStateFlow(0L)

    override val clipboardTimeoutSeconds: Flow<Int> = _clipboardTimeoutSeconds
    override val sntpEnabled: Flow<Boolean> = _sntpEnabled
    override val theme: Flow<String> = _theme
    override val biometricEnabled: Flow<Boolean> = _biometricEnabled
    override val lastBackupTimestamp: Flow<Long> = _lastBackupTimestamp

    override suspend fun setClipboardTimeout(seconds: Int) {
        _clipboardTimeoutSeconds.value = seconds
    }

    override suspend fun setSntpEnabled(enabled: Boolean) {
        _sntpEnabled.value = enabled
    }

    override suspend fun setTheme(theme: String) {
        _theme.value = theme
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
    }

    override suspend fun setLastBackupTimestamp(millis: Long) {
        _lastBackupTimestamp.value = millis
    }
}
