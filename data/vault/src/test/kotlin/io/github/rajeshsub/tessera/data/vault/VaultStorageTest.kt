package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.crypto.KdfParams
import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class VaultStorageTest {
    private lateinit var tmpFile: File
    private val key = ByteArray(32) { it.toByte() }
    private val salt = ByteArray(16) { it.toByte() }
    private val params = KdfParams(mCostKibibytes = 1, tCostIterations = 1, parallelism = 1)

    @Before fun setup() {
        tmpFile = File.createTempFile("vault_storage_test", ".bin")
        tmpFile.delete()
    }

    @After fun teardown() {
        tmpFile.delete()
        File(tmpFile.parentFile, "${tmpFile.name}.tmp").delete()
    }

    @Test fun existsReturnsFalseBeforeSave() {
        assertFalse(VaultStorage(tmpFile).exists())
    }

    @Test fun existsReturnsTrueAfterSave() {
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, emptyList())
        assertTrue(storage.exists())
    }

    @Test fun roundTripEmptyList() {
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, emptyList())
        assertTrue(storage.readAndDecryptAccounts(key).isEmpty())
    }

    @Test fun roundTripSingleAccount() {
        val account =
            Account(
                id = "t1",
                type = OtpType.TOTP,
                label = "Test",
                issuer = null,
                secret = byteArrayOf(1, 2, 3, 4, 5),
            )
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, listOf(account))
        val loaded = storage.readAndDecryptAccounts(key)
        assertEquals(1, loaded.size)
        assertEquals(account, loaded[0])
    }

    @Test fun readHeaderReturnsSalt() {
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, emptyList())
        assertTrue(storage.readHeader().salt.contentEquals(salt))
    }

    @Test fun readHeaderReturnsParams() {
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, emptyList())
        assertEquals(params, storage.readHeader().params)
    }

    @Test fun wrongKeyThrowsOnDecrypt() {
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, emptyList())
        val wrongKey = ByteArray(32) { 0xFF.toByte() }
        var threw = false
        try {
            storage.readAndDecryptAccounts(wrongKey)
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
        ) {
            threw = true
        }
        assertTrue("Expected authentication failure with wrong key", threw)
    }

    @Test fun atomicWriteLeavesNoTmpFile() {
        VaultStorage(tmpFile).saveAccounts(key, salt, params, emptyList())
        assertFalse(File(tmpFile.parentFile, "${tmpFile.name}.tmp").exists())
    }

    @Test fun overwriteRoundTrips() {
        val a1 = Account(id = "a1", type = OtpType.TOTP, label = "A1", issuer = null, secret = byteArrayOf(1))
        val a2 = Account(id = "a2", type = OtpType.TOTP, label = "A2", issuer = null, secret = byteArrayOf(2))
        val storage = VaultStorage(tmpFile)
        storage.saveAccounts(key, salt, params, listOf(a1))
        storage.saveAccounts(key, salt, params, listOf(a1, a2))
        val loaded = storage.readAndDecryptAccounts(key)
        assertEquals(2, loaded.size)
    }
}
