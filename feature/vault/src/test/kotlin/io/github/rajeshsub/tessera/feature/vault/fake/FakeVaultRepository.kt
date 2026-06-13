package io.github.rajeshsub.tessera.feature.vault.fake

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.HotpGenerator
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeVaultRepository : VaultRepository {
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    override val accounts: StateFlow<List<Account>> = _accounts

    private val _isUnlocked = MutableStateFlow(false)
    override val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private var _exists = false
    override val exists: Boolean get() = _exists

    var unlockShouldSucceed = true
    val unlockCalls = mutableListOf<CharArray>()

    override suspend fun createVault(password: CharArray) {
        _exists = true
        _isUnlocked.value = true
        unlockCalls.add(password)
    }

    override suspend fun unlock(password: CharArray): Boolean {
        unlockCalls.add(password)
        return if (unlockShouldSucceed) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    override fun lock() {
        _isUnlocked.value = false
        _accounts.value = emptyList()
    }

    override suspend fun addAccount(account: Account) {
        _accounts.value = _accounts.value + account
    }

    override suspend fun updateAccount(account: Account) {
        _accounts.value = _accounts.value.map { if (it.id == account.id) account else it }
    }

    override suspend fun deleteAccount(id: String) {
        _accounts.value = _accounts.value.filter { it.id != id }
    }

    override suspend fun advanceHotpCounter(id: String): String {
        val idx = _accounts.value.indexOfFirst { it.id == id }
        require(idx >= 0)
        val account = _accounts.value[idx]
        require(account.type == OtpType.HOTP)
        val newCounter = account.counter + 1
        _accounts.value = _accounts.value.toMutableList().also { it[idx] = account.copy(counter = newCounter) }
        return HotpGenerator.generate(account.secret, newCounter, account.digits, account.algorithm)
    }

    fun seed(accounts: List<Account>) {
        _accounts.value = accounts
    }

    fun setExists(v: Boolean) {
        _exists = v
    }
}
