package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.crypto.KdfParams
import io.github.rajeshsub.tessera.core.crypto.VaultKdf
import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.HotpGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom

@Suppress("TooManyFunctions")
internal class VaultRepositoryImpl(
    private val storage: VaultStorage,
    private val kdfDerive: (CharArray, ByteArray, KdfParams) -> ByteArray = VaultKdf::derive,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VaultRepository {
    private val lock = Mutex()
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    private val _isUnlocked = MutableStateFlow(false)

    override val accounts: StateFlow<List<Account>> = _accounts
    override val isUnlocked: StateFlow<Boolean> = _isUnlocked
    override val exists: Boolean get() = storage.exists()

    @Volatile private var unlockedKey: ByteArray? = null

    @Volatile private var currentSalt: ByteArray? = null

    @Volatile private var currentParams: KdfParams? = null

    override suspend fun createVault(password: CharArray): Unit =
        lock.withLock {
            withContext(ioDispatcher) {
                val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                val params = VaultKdf.PROD_PARAMS
                val key = kdfDerive(password, salt, params)
                storage.saveAccounts(key, salt, params, emptyList())
                unlockedKey = key
                currentSalt = salt
                currentParams = params
            }
            _accounts.value = emptyList()
            _isUnlocked.value = true
            Timber.d("Vault created")
        }

    override suspend fun unlock(password: CharArray): Boolean =
        lock.withLock {
            if (!storage.exists()) return@withLock false
            try {
                withContext(ioDispatcher) {
                    val header = storage.readHeader()
                    val key = kdfDerive(password, header.salt, header.params)
                    val accountList = storage.readAndDecryptAccounts(key)
                    unlockedKey = key
                    currentSalt = header.salt
                    currentParams = header.params
                    _accounts.value = accountList
                    _isUnlocked.value = true
                    Timber.d("Vault unlocked: %d accounts", accountList.size)
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                Timber.w(e, "Vault unlock failed")
                false
            }
        }

    override fun lock() {
        unlockedKey?.fill(0)
        unlockedKey = null
        currentSalt = null
        currentParams = null
        _accounts.value = emptyList()
        _isUnlocked.value = false
        Timber.d("Vault locked")
    }

    override suspend fun addAccount(account: Account): Unit =
        lock.withLock {
            val key = requireUnlocked()
            val newList = _accounts.value + account
            withContext(ioDispatcher) { persist(key, newList) }
            _accounts.value = newList
            Timber.d("Account added: id=%s", account.id)
        }

    override suspend fun updateAccount(account: Account): Unit =
        lock.withLock {
            val key = requireUnlocked()
            val newList = _accounts.value.map { if (it.id == account.id) account else it }
            withContext(ioDispatcher) { persist(key, newList) }
            _accounts.value = newList
            Timber.d("Account updated: id=%s", account.id)
        }

    override suspend fun deleteAccount(id: String): Unit =
        lock.withLock {
            val key = requireUnlocked()
            val newList = _accounts.value.filter { it.id != id }
            withContext(ioDispatcher) { persist(key, newList) }
            _accounts.value = newList
            Timber.d("Account deleted: id=%s", id)
        }

    override suspend fun advanceHotpCounter(id: String): String =
        lock.withLock {
            val key = requireUnlocked()
            val mutable = _accounts.value.toMutableList()
            val idx = mutable.indexOfFirst { it.id == id }
            require(idx >= 0) { "Account not found: $id" }
            val account = mutable[idx]
            require(account.type == OtpType.HOTP) { "Not an HOTP account: $id" }

            val newCounter = account.counter + 1
            mutable[idx] = account.copy(counter = newCounter)

            // Persist counter BEFORE returning the code (RFC 4226 §7.2)
            withContext(ioDispatcher) { persist(key, mutable) }
            _accounts.value = mutable

            Timber.d("HOTP counter advanced: id=%s counter=%d", id, newCounter)
            HotpGenerator.generate(account.secret, newCounter, account.digits, account.algorithm)
        }

    private fun requireUnlocked(): ByteArray = unlockedKey ?: error("Vault is locked")

    private fun persist(
        key: ByteArray,
        accounts: List<Account>,
    ) {
        val salt = currentSalt ?: error("No salt available")
        val params = currentParams ?: error("No params available")
        storage.saveAccounts(key, salt, params, accounts)
    }
}
