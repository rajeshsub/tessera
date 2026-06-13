package io.github.rajeshsub.tessera.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultMigratorTest {
    private val header = VaultHeader(KdfParams(65_536, 3, 4), ByteArray(16), ByteArray(12))
    private val ciphertext = ByteArray(10) { it.toByte() }

    @Test fun currentVersionIs1() {
        assertEquals(1, VaultMigrator.CURRENT_VERSION)
    }

    @Test fun noMigrationNeededForCurrentVersion() {
        assertFalse(VaultMigrator.needsMigration(1))
    }

    @Test fun v1IsNoOp() {
        val data = VaultMigrator.VaultData(header, ciphertext)
        val (newVersion, newData) = VaultMigrator.migrate(1, data)
        assertEquals(1, newVersion)
        assertEquals(header, newData.header)
        assertArrayEquals(ciphertext, newData.ciphertext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownVersionAboveCurrent() {
        VaultMigrator.migrate(99, VaultMigrator.VaultData(header, ciphertext))
    }

    @Test(expected = IllegalArgumentException::class)
    fun versionZeroInvalid() {
        VaultMigrator.migrate(0, VaultMigrator.VaultData(header, ciphertext))
    }

    @Test fun needsMigrationTrueForVersionBelowCurrent() {
        // needsMigration is a predicate; it doesn't validate the version itself
        assertTrue(VaultMigrator.needsMigration(0))
    }
}
