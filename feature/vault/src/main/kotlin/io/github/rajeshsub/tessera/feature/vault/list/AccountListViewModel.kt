package io.github.rajeshsub.tessera.feature.vault.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.TotpGenerator
import io.github.rajeshsub.tessera.data.vault.PrefsRepository
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountListItem(
    val account: Account,
    val currentCode: String,
    val secondsRemaining: Int,
    val isRevealed: Boolean,
)

@HiltViewModel
class AccountListViewModel
    @Inject
    constructor(
        val vaultRepository: VaultRepository,
        private val prefsRepository: PrefsRepository,
    ) : ViewModel() {
        sealed interface UiEffect {
            data class CopyToClipboard(
                val code: String,
                val timeoutMs: Long,
            ) : UiEffect

            data class ShowSnackbar(
                val message: String,
            ) : UiEffect
        }

        private val _items = MutableStateFlow<List<AccountListItem>>(emptyList())
        val items: StateFlow<List<AccountListItem>> = _items

        private val _effects = Channel<UiEffect>(capacity = Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        val isUnlocked: StateFlow<Boolean> =
            vaultRepository.isUnlocked
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

        private val revealedIds = mutableSetOf<String>()

        init {
            viewModelScope.launch {
                vaultRepository.accounts.collect { accounts ->
                    rebuildItems(accounts)
                }
            }
            // Live countdown ticker. Gated on UI subscription so it only runs while
            // the list is observed; without subscribers it stays idle (and unit tests
            // that merely read items.value do not spin runTest's scheduler forever).
            viewModelScope.launch {
                _items.subscriptionCount
                    .map { it > 0 }
                    .distinctUntilChanged()
                    .collectLatest { hasSubscribers ->
                        while (hasSubscribers) {
                            rebuildItems(vaultRepository.accounts.value)
                            delay(TICK_MS)
                        }
                    }
            }
        }

        private fun rebuildItems(accounts: List<Account>) {
            val nowSec = System.currentTimeMillis() / 1_000L
            _items.value =
                accounts.map { account ->
                    val code =
                        if (account.type == OtpType.TOTP) {
                            TotpGenerator.generate(
                                account.secret,
                                nowSec,
                                account.digits,
                                account.period,
                                account.algorithm,
                            )
                        } else {
                            ""
                        }
                    AccountListItem(
                        account = account,
                        currentCode = code,
                        secondsRemaining =
                            if (account.type == OtpType.TOTP) {
                                TotpGenerator.secondsRemaining(nowSec, account.period)
                            } else {
                                0
                            },
                        isRevealed = account.id in revealedIds,
                    )
                }
        }

        fun toggleReveal(accountId: String) {
            if (accountId in revealedIds) {
                revealedIds.remove(accountId)
            } else {
                revealedIds.add(accountId)
            }
            rebuildItems(vaultRepository.accounts.value)
        }

        fun copyCode(accountId: String) {
            viewModelScope.launch {
                val item = _items.value.find { it.account.id == accountId } ?: return@launch
                val code =
                    if (item.account.type == OtpType.HOTP) {
                        vaultRepository.advanceHotpCounter(accountId)
                    } else {
                        item.currentCode
                    }
                val timeoutSeconds = prefsRepository.clipboardTimeoutSeconds.first()
                _effects.send(UiEffect.CopyToClipboard(code, timeoutSeconds * 1_000L))
            }
        }

        fun advanceHotp(accountId: String) {
            viewModelScope.launch {
                try {
                    val code = vaultRepository.advanceHotpCounter(accountId)
                    revealedIds.add(accountId)
                    rebuildItems(vaultRepository.accounts.value)
                    val timeoutSeconds = prefsRepository.clipboardTimeoutSeconds.first()
                    _effects.send(UiEffect.CopyToClipboard(code, timeoutSeconds * 1_000L))
                } catch (
                    @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
                ) {
                    _effects.send(UiEffect.ShowSnackbar("Failed to generate code"))
                }
            }
        }

        fun deleteAccount(accountId: String) {
            viewModelScope.launch {
                vaultRepository.deleteAccount(accountId)
            }
        }

        fun lock() {
            vaultRepository.lock()
        }

        private companion object {
            const val TICK_MS = 200L
        }
    }
