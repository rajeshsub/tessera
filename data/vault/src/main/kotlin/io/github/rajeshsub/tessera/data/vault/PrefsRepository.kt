package io.github.rajeshsub.tessera.data.vault

import kotlinx.coroutines.flow.Flow

interface PrefsRepository {
    val clipboardTimeoutSeconds: Flow<Int>
    val sntpEnabled: Flow<Boolean>
    val theme: Flow<String>
    val biometricEnabled: Flow<Boolean>
    val lastBackupTimestamp: Flow<Long>

    suspend fun setClipboardTimeout(seconds: Int)

    suspend fun setSntpEnabled(enabled: Boolean)

    suspend fun setTheme(theme: String)

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun setLastBackupTimestamp(millis: Long)
}
