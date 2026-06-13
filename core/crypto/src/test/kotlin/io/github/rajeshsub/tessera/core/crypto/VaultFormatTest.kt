package io.github.rajeshsub.tessera.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultFormatTest {
    private val header =
        VaultHeader(
            params = KdfParams(65_536, 3, 4),
            salt = ByteArray(16) { it.toByte() },
            iv = ByteArray(12) { (it + 100).toByte() },
        )
    private val ciphertext = ByteArray(48) { (it * 5).toByte() }

    @Test fun roundTrip() {
        val bytes = VaultFormat.encode(header, ciphertext)
        val (version, decodedHeader, decodedCt) = VaultFormat.decode(bytes)
        assertEquals(1, version)
        assertEquals(header, decodedHeader)
        assertArrayEquals(ciphertext, decodedCt)
    }

    @Test fun magicBytesArePresent() {
        val bytes = VaultFormat.encode(header, ciphertext)
        // Magic = "TESS"
        assertEquals('T'.code.toByte(), bytes[0])
        assertEquals('E'.code.toByte(), bytes[1])
        assertEquals('S'.code.toByte(), bytes[2])
        assertEquals('S'.code.toByte(), bytes[3])
    }

    @Test fun versionByteIsOne() {
        val bytes = VaultFormat.encode(header, ciphertext)
        assertEquals(1.toByte(), bytes[4])
    }

    @Test fun encodedSizeIsCorrect() {
        val bytes = VaultFormat.encode(header, ciphertext)
        // 4 magic + 1 version + 2 headerLen + 34 header + ciphertext.size
        assertEquals(4 + 1 + 2 + 34 + ciphertext.size, bytes.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeThrowsOnBadMagic() {
        val bytes = VaultFormat.encode(header, ciphertext)
        bytes[0] = 0x00
        VaultFormat.decode(bytes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeThrowsOnUnknownVersion() {
        val bytes = VaultFormat.encode(header, ciphertext)
        bytes[4] = 99
        VaultFormat.decode(bytes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeThrowsOnTooShort() {
        VaultFormat.decode(ByteArray(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeThrowsOnTruncatedHeader() {
        // Valid magic + version + header-length=34, but only 5 bytes of data follow (< 34)
        val bytes = ByteArray(4 + 1 + 2 + 5)
        bytes[0] = 'T'.code.toByte()
        bytes[1] = 'E'.code.toByte()
        bytes[2] = 'S'.code.toByte()
        bytes[3] = 'S'.code.toByte()
        bytes[4] = 1 // version
        bytes[5] = 0
        bytes[6] = 34 // header length = 34 in big-endian
        VaultFormat.decode(bytes)
    }

    // VaultHeader equality and hash
    @Test fun vaultHeaderEqualsSameContent() {
        val h1 = VaultHeader(KdfParams(1, 2, 3), byteArrayOf(1, 2), byteArrayOf(3, 4))
        val h2 = VaultHeader(KdfParams(1, 2, 3), byteArrayOf(1, 2), byteArrayOf(3, 4))
        assertEquals(h1, h2)
    }

    @Test fun vaultHeaderEqualsSameRef() {
        assertTrue(header == header)
    }

    @Test fun vaultHeaderNotEqualsOtherType() {
        assertFalse(header.equals("not a header"))
    }

    @Test fun vaultHeaderHashCodeConsistent() {
        assertEquals(header.hashCode(), header.hashCode())
    }

    @Test fun vaultHeaderHashCodeDiffersOnSaltChange() {
        val h2 = header.copy(salt = ByteArray(16) { 0xFF.toByte() })
        assertNotEquals(header.hashCode(), h2.hashCode())
    }

    @Test fun vaultHeaderHashCodeDiffersOnIvChange() {
        val h2 = header.copy(iv = ByteArray(12) { 0xFF.toByte() })
        assertNotEquals(header.hashCode(), h2.hashCode())
    }
}
