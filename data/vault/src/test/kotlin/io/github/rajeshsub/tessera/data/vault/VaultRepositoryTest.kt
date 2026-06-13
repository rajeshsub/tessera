package io.github.rajeshsub.tessera.data.vault

import app.cash.turbine.test
import io.github.rajeshsub.tessera.core.crypto.KdfParams
import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class VaultRepositoryTest {
    private lateinit var tmpFile: File
    private val testKey = ByteArray(32) { it.toByte() }
    private val mockKdf: (CharArray, ByteArray, KdfParams) -> ByteArray = { _, _, _ -> testKey.copyOf() }

    @Before fun setup() {
        tmpFile = File.createTempFile("repo_test", ".bin")
        tmpFile.delete()
    }

    @After fun teardown() {
        tmpFile.delete()
        File(tmpFile.parentFile, "${tmpFile.name}.tmp").delete()
    }

    @Suppress("InjectDispatcher")
    private fun makeRepo(): VaultRepositoryImpl =
        VaultRepositoryImpl(
            storage = VaultStorage(tmpFile),
            kdfDerive = mockKdf,
            ioDispatcher = Dispatchers.Unconfined,
        )

    @Test fun existsFalseBeforeCreate() =
        runTest {
            assertFalse(makeRepo().exists)
        }

    @Test fun createVaultSetsUnlocked() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            assertTrue(repo.isUnlocked.value)
            assertTrue(repo.exists)
            assertTrue(repo.accounts.value.isEmpty())
        }

    @Test fun unlockReturnsFalseWhenNoVault() =
        runTest {
            assertFalse(makeRepo().unlock(charArrayOf('p')))
        }

    @Test fun lockClearsState() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.lock()
            assertFalse(repo.isUnlocked.value)
            assertTrue(repo.accounts.value.isEmpty())
        }

    @Test fun createThenLockThenUnlock() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.lock()
            assertTrue(repo.unlock(charArrayOf('p')))
            assertTrue(repo.isUnlocked.value)
        }

    @Test fun addAccountPersistsAcrossReload() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.addAccount(makeAccount("a1"))
            assertEquals(1, repo.accounts.value.size)

            repo.lock()
            repo.unlock(charArrayOf('p'))
            assertEquals(1, repo.accounts.value.size)
            assertEquals("a1", repo.accounts.value[0].id)
        }

    @Test fun updateAccountPersistsAcrossReload() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.addAccount(makeAccount("a1"))
            repo.updateAccount(makeAccount("a1").copy(label = "updated"))
            assertEquals("updated", repo.accounts.value[0].label)

            repo.lock()
            repo.unlock(charArrayOf('p'))
            assertEquals("updated", repo.accounts.value[0].label)
        }

    @Test fun deleteAccountPersistsAcrossReload() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.addAccount(makeAccount("a1"))
            repo.addAccount(makeAccount("a2"))
            repo.deleteAccount("a1")
            assertEquals(1, repo.accounts.value.size)
            assertEquals("a2", repo.accounts.value[0].id)

            repo.lock()
            repo.unlock(charArrayOf('p'))
            assertEquals(1, repo.accounts.value.size)
        }

    @Test fun advanceHotpCounterPersistsBeforeReturn() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            val hotp =
                Account(
                    id = "h1",
                    type = OtpType.HOTP,
                    label = "hotp",
                    issuer = null,
                    // RFC 4226 reference secret "12345678901234567890"
                    secret = "12345678901234567890".toByteArray(Charsets.US_ASCII),
                    counter = 0L,
                )
            repo.addAccount(hotp)
            val code = repo.advanceHotpCounter("h1")
            assertNotNull(code)
            assertEquals(6, code.length)
            assertEquals(1L, repo.accounts.value[0].counter)

            repo.lock()
            repo.unlock(charArrayOf('p'))
            assertEquals(1L, repo.accounts.value[0].counter)
        }

    @Test fun accountsFlowEmitsOnAdd() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repo.accounts.test {
                assertTrue(awaitItem().isEmpty())
                repo.addAccount(makeAccount("x1"))
                assertEquals(1, awaitItem().size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test fun multipleAccountsRoundTrip() =
        runTest {
            val repo = makeRepo()
            repo.createVault(charArrayOf('p'))
            repeat(5) { repo.addAccount(makeAccount("id$it")) }
            assertEquals(5, repo.accounts.value.size)

            repo.lock()
            repo.unlock(charArrayOf('p'))
            assertEquals(5, repo.accounts.value.size)
        }

    private fun makeAccount(id: String) =
        Account(
            id = id,
            type = OtpType.TOTP,
            label = "label-$id",
            issuer = "Issuer",
            secret = ByteArray(20) { it.toByte() },
        )
}
