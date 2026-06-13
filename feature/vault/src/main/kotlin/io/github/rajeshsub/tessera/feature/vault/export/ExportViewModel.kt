package io.github.rajeshsub.tessera.feature.vault.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.data.vault.OtpAuthUriExporter
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class ExportViewModel
    @Inject
    constructor(
        private val vaultRepository: VaultRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data object ExportDone : UiEffect

            data class Error(
                val message: String,
            ) : UiEffect
        }

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        fun exportPlaintext(outputStream: OutputStream) {
            viewModelScope.launch {
                runCatching {
                    val uris =
                        vaultRepository.accounts.value
                            .joinToString("\n") { OtpAuthUriExporter.build(it) }
                    outputStream.use { it.write(uris.toByteArray(Charsets.UTF_8)) }
                    _effects.send(UiEffect.ExportDone)
                }.onFailure {
                    _effects.send(UiEffect.Error("Export failed"))
                }
            }
        }
    }
