package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import kotlinx.coroutines.flow.StateFlow

interface VaultRepository {
    val accounts: StateFlow<List<Account>>
    val isUnlocked: StateFlow<Boolean>
    val exists: Boolean

    suspend fun createVault(password: CharArray)

    suspend fun unlock(password: CharArray): Boolean

    fun lock()

    suspend fun addAccount(account: Account)

    suspend fun updateAccount(account: Account)

    suspend fun deleteAccount(id: String)

    /** Increments the HOTP counter, persists it atomically, then returns the new code. */
    suspend fun advanceHotpCounter(id: String): String
}
