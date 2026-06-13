package io.github.rajeshsub.tessera.feature.vault.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.data.vault.PrefsRepository
import io.github.rajeshsub.tessera.data.vault.SafBackupManager
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel
    @Inject
    constructor(
        private val safBackupManager: SafBackupManager,
        private val vaultRepository: VaultRepository,
        private val prefsRepository: PrefsRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data object BackupDone : UiEffect

            data object RestoreDone : UiEffect

            data class Error(
                val message: String,
            ) : UiEffect
        }

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        fun backup(outputStream: OutputStream) {
            viewModelScope.launch {
                runCatching {
                    safBackupManager.backup(outputStream)
                    prefsRepository.setLastBackupTimestamp(System.currentTimeMillis())
                    _effects.send(UiEffect.BackupDone)
                }.onFailure {
                    _effects.send(UiEffect.Error("Backup failed: ${it.message}"))
                }
            }
        }

        fun restore(inputStream: InputStream) {
            viewModelScope.launch {
                runCatching {
                    val success = safBackupManager.restore(inputStream)
                    if (success) {
                        vaultRepository.lock()
                        _effects.send(UiEffect.RestoreDone)
                    } else {
                        _effects.send(UiEffect.Error("Restore failed"))
                    }
                }.onFailure {
                    _effects.send(UiEffect.Error("Restore failed: invalid vault file"))
                }
            }
        }
    }
