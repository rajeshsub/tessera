package io.github.rajeshsub.tessera.feature.vault.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.Base32
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import io.github.rajeshsub.tessera.feature.vault.navigation.EditAccount
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAccountViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val vaultRepository: VaultRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data object Saved : UiEffect

            data object Deleted : UiEffect

            data class Error(
                val message: String,
            ) : UiEffect
        }

        data class FormState(
            val label: String = "",
            val issuer: String = "",
            val secret: String = "",
            val type: OtpType = OtpType.TOTP,
            val algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
            val digits: Int = 6,
            val period: Int = 30,
            val counter: Long = 0L,
        )

        private val accountId: String = checkNotNull(savedStateHandle[EditAccount::accountId.name])

        private val _form = MutableStateFlow(FormState())
        val form: StateFlow<FormState> = _form

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        init {
            viewModelScope.launch {
                val account = vaultRepository.accounts.first().find { it.id == accountId }
                if (account != null) {
                    _form.value =
                        FormState(
                            label = account.label,
                            issuer = account.issuer ?: "",
                            secret = Base32.encode(account.secret).trimEnd('='),
                            type = account.type,
                            algorithm = account.algorithm,
                            digits = account.digits,
                            period = account.period,
                            counter = account.counter,
                        )
                }
            }
        }

        fun updateLabel(v: String) {
            _form.value = _form.value.copy(label = v)
        }

        fun updateIssuer(v: String) {
            _form.value = _form.value.copy(issuer = v)
        }

        fun updateType(v: OtpType) {
            _form.value = _form.value.copy(type = v)
        }

        fun updateAlgorithm(v: OtpAlgorithm) {
            _form.value = _form.value.copy(algorithm = v)
        }

        fun updateDigits(v: Int) {
            _form.value = _form.value.copy(digits = v)
        }

        fun updatePeriod(v: Int) {
            _form.value = _form.value.copy(period = v)
        }

        fun updateCounter(v: Long) {
            _form.value = _form.value.copy(counter = v)
        }

        fun save() {
            viewModelScope.launch {
                val f = _form.value
                if (f.label.isBlank()) {
                    _effects.send(UiEffect.Error("Label is required"))
                    return@launch
                }
                val secretBytes =
                    runCatching { Base32.decode(f.secret) }.getOrElse {
                        _effects.send(UiEffect.Error("Invalid Base32 secret"))
                        return@launch
                    }
                val existing =
                    vaultRepository.accounts.first().find { it.id == accountId }
                        ?: run {
                            _effects.send(UiEffect.Error("Account not found"))
                            return@launch
                        }
                vaultRepository.updateAccount(
                    existing.copy(
                        label = f.label.trim(),
                        issuer = f.issuer.trim().takeIf { it.isNotEmpty() },
                        secret = secretBytes,
                        algorithm = f.algorithm,
                        digits = f.digits,
                        period = f.period,
                        counter = f.counter,
                    ),
                )
                _effects.send(UiEffect.Saved)
            }
        }

        fun delete() {
            viewModelScope.launch {
                vaultRepository.deleteAccount(accountId)
                _effects.send(UiEffect.Deleted)
            }
        }
    }
