package io.github.rajeshsub.tessera.feature.vault.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.data.vault.BiometricKeystore
import io.github.rajeshsub.tessera.data.vault.PrefsRepository
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val prefsRepository: PrefsRepository,
        val biometricKeystore: BiometricKeystore,
        private val vaultRepository: VaultRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data class PromptBiometric(
                val cipher: Cipher,
            ) : UiEffect

            data object BiometricEnrolled : UiEffect

            data object BiometricDisabled : UiEffect

            data class Error(
                val message: String,
            ) : UiEffect
        }

        val clipboardTimeout: StateFlow<Int> =
            prefsRepository.clipboardTimeoutSeconds
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)

        val sntpEnabled: StateFlow<Boolean> =
            prefsRepository.sntpEnabled
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        val theme: StateFlow<String> =
            prefsRepository.theme
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

        val biometricEnabled: StateFlow<Boolean> =
            prefsRepository.biometricEnabled
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        val lastBackupTimestamp: StateFlow<Long> =
            prefsRepository.lastBackupTimestamp
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

        private val _enrollBusy = MutableStateFlow(false)
        val enrollBusy: StateFlow<Boolean> = _enrollBusy

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        fun setClipboardTimeout(seconds: Int) {
            viewModelScope.launch { prefsRepository.setClipboardTimeout(seconds) }
        }

        fun setSntpEnabled(enabled: Boolean) {
            viewModelScope.launch { prefsRepository.setSntpEnabled(enabled) }
        }

        fun setTheme(theme: String) {
            viewModelScope.launch { prefsRepository.setTheme(theme) }
        }

        fun enrollBiometric(password: CharArray) {
            viewModelScope.launch {
                _enrollBusy.value = true
                try {
                    val valid = vaultRepository.unlock(password)
                    if (!valid) {
                        _effects.send(UiEffect.Error("Invalid passphrase"))
                        return@launch
                    }
                    runCatching {
                        biometricKeystore.generateKey()
                        val cipher = biometricKeystore.getEncryptCipher()
                        _effects.send(UiEffect.PromptBiometric(cipher))
                    }.onFailure {
                        _effects.send(UiEffect.Error("Failed to initialize biometric"))
                    }
                } finally {
                    _enrollBusy.value = false
                }
            }
        }

        fun finishBiometricEnrollment(
            cipher: Cipher,
            password: CharArray,
        ) {
            viewModelScope.launch {
                runCatching {
                    biometricKeystore.saveEncryptedPassword(cipher, password)
                    prefsRepository.setBiometricEnabled(true)
                    _effects.send(UiEffect.BiometricEnrolled)
                }.onFailure {
                    _effects.send(UiEffect.Error("Failed to save biometric credential"))
                }
            }
        }

        fun disableBiometric() {
            viewModelScope.launch {
                biometricKeystore.deleteEnrollment()
                prefsRepository.setBiometricEnabled(false)
                _effects.send(UiEffect.BiometricDisabled)
            }
        }
    }
