package io.github.rajeshsub.tessera.feature.vault.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.Base32
import io.github.rajeshsub.tessera.core.otp.OtpAuthUri
import io.github.rajeshsub.tessera.data.vault.OtpMigrationParser
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel
    @Inject
    constructor(
        private val vaultRepository: VaultRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data object Saved : UiEffect

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

        private val _form = MutableStateFlow(FormState())
        val form: StateFlow<FormState> = _form

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        fun updateLabel(v: String) {
            _form.value = _form.value.copy(label = v)
        }

        fun updateIssuer(v: String) {
            _form.value = _form.value.copy(issuer = v)
        }

        fun updateSecret(v: String) {
            _form.value = _form.value.copy(secret = v.uppercase().trim())
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

        fun handleScannedUri(uri: String) {
            val accounts = OtpMigrationParser.parse(uri)
            if (accounts.isNotEmpty()) {
                viewModelScope.launch {
                    accounts.forEach { vaultRepository.addAccount(it) }
                    _effects.send(UiEffect.Saved)
                }
                return
            }
            val params = OtpAuthUri.parse(uri)
            if (params != null) {
                _form.value =
                    FormState(
                        label = params.label,
                        issuer = params.issuer ?: "",
                        secret = Base32.encode(params.secret).trimEnd('='),
                        type = params.type,
                        algorithm = params.algorithm,
                        digits = params.digits,
                        period = params.period,
                        counter = params.counter,
                    )
            } else {
                viewModelScope.launch {
                    _effects.send(UiEffect.Error("Unrecognized QR code"))
                }
            }
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
                val account =
                    Account(
                        id = UUID.randomUUID().toString(),
                        type = f.type,
                        label = f.label.trim(),
                        issuer = f.issuer.trim().takeIf { it.isNotEmpty() },
                        secret = secretBytes,
                        algorithm = f.algorithm,
                        digits = f.digits,
                        period = f.period,
                        counter = f.counter,
                    )
                vaultRepository.addAccount(account)
                _effects.send(UiEffect.Saved)
            }
        }
    }
