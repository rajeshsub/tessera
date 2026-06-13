package io.github.rajeshsub.tessera.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AES-256-GCM known-answer test. Vectors computed with JVM AES/GCM/NoPadding (FIPS-certified).
 * Key and IV are from the GCM spec example (feffe9... / cafebabe...).
 */
class AesGcmCipherTest {
    private val key = hexToBytes("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308")
    private val iv = hexToBytes("cafebabefacedbaddecaf888")
    private val pt =
        hexToBytes(
            "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b39",
        )

    // CT + 16-byte GCM auth tag, computed deterministically by JVM JCE
    private val ctWithTag =
        hexToBytes(
            "522dc1f099567d07f47f37a32a84427d" +
                "643a8cdcbfe5c0c97598a2bd2555d1aa" +
                "8cb08e48590dbb3da7b08b1056828838" +
                "c5f61e6393ba7a0abcc9f662" +
                "eb9f796c8d356fc31a8433884b696f4f",
        )

    @Test fun knownAnswerEncrypt() {
        val result = AesGcmCipher.encrypt(key, iv, pt)
        assertArrayEquals(ctWithTag, result)
    }

    @Test fun knownAnswerDecrypt() {
        val result = AesGcmCipher.decrypt(key, iv, ctWithTag)
        assertArrayEquals(pt, result)
    }

    @Test fun roundTrip() {
        val plaintext = "Hello, Tessera!".toByteArray()
        val testKey = ByteArray(32) { it.toByte() }
        val testIv = ByteArray(12) { (it + 100).toByte() }
        val ct = AesGcmCipher.encrypt(testKey, testIv, plaintext)
        val recovered = AesGcmCipher.decrypt(testKey, testIv, ct)
        assertArrayEquals(plaintext, recovered)
    }

    @Test fun ciphertextLengthIsPlaintextPlusTag() {
        val plaintext = ByteArray(20)
        val ct = AesGcmCipher.encrypt(ByteArray(32), ByteArray(12), plaintext)
        assertEquals(plaintext.size + 16, ct.size) // 16-byte auth tag
    }

    @Test(expected = Exception::class)
    fun decryptWithWrongKeyThrows() {
        val wrongKey = ByteArray(32) { 0xFF.toByte() }
        AesGcmCipher.decrypt(wrongKey, iv, ctWithTag)
    }

    @Test(expected = IllegalArgumentException::class)
    fun encryptWithShortKeyThrows() {
        AesGcmCipher.encrypt(ByteArray(16), iv, pt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun encryptWithWrongIvLengthThrows() {
        AesGcmCipher.encrypt(key, ByteArray(8), pt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decryptWithShortKeyThrows() {
        AesGcmCipher.decrypt(ByteArray(16), iv, ctWithTag)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decryptWithWrongIvLengthThrows() {
        AesGcmCipher.decrypt(key, ByteArray(8), ctWithTag)
    }

    private fun hexToBytes(s: String): ByteArray {
        val d = ByteArray(s.length / 2)
        for (i in d.indices) d[i] = ((Character.digit(s[i * 2], 16) shl 4) + Character.digit(s[i * 2 + 1], 16)).toByte()
        return d
    }
}
