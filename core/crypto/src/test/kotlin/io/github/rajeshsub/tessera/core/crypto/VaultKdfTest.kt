package io.github.rajeshsub.tessera.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * VaultKdf unit tests. Uses a fake [ArgonHasher] so the argon2kt native library
 * is never loaded. The actual Argon2id computation is tested via instrumented tests.
 */
class VaultKdfTest {
    private val salt = ByteArray(16) { it.toByte() }
    private val testParams = KdfParams(mCostKibibytes = 64, tCostIterations = 1, parallelism = 1)

    @Test fun delegatesToHasher() {
        val expected = ByteArray(32) { (it + 1).toByte() }
        val hasher = ArgonHasher { _, _, _ -> expected }
        val result = VaultKdf.deriveInternal(charArrayOf('p', 'w'), salt, testParams, hasher)
        assertArrayEquals(expected, result)
    }

    @Test fun passesPasswordAsUtf8Bytes() {
        var capturedPassword: ByteArray? = null
        val hasher =
            ArgonHasher { pw, _, _ ->
                capturedPassword = pw.copyOf()
                ByteArray(32)
            }
        VaultKdf.deriveInternal(charArrayOf('A', 'B', 'C'), salt, testParams, hasher)
        assertArrayEquals("ABC".toByteArray(Charsets.UTF_8), capturedPassword)
    }

    @Test fun zerosPasswordBytesAfterHash() {
        var passwordRef: ByteArray? = null
        val hasher =
            ArgonHasher { pw, _, _ ->
                passwordRef = pw // capture reference (not copy)
                ByteArray(32)
            }
        VaultKdf.deriveInternal(charArrayOf('s', 'e', 'c'), salt, testParams, hasher)
        assertTrue("Password bytes must be zeroed after hash", passwordRef!!.all { it == 0.toByte() })
    }

    @Test fun passesSaltToHasher() {
        var capturedSalt: ByteArray? = null
        val hasher =
            ArgonHasher { _, s, _ ->
                capturedSalt = s
                ByteArray(32)
            }
        VaultKdf.deriveInternal(charArrayOf('x'), salt, testParams, hasher)
        assertArrayEquals(salt, capturedSalt)
    }

    @Test fun passesParamsToHasher() {
        var capturedParams: KdfParams? = null
        val hasher =
            ArgonHasher { _, _, p ->
                capturedParams = p
                ByteArray(32)
            }
        VaultKdf.deriveInternal(charArrayOf('p'), salt, testParams, hasher)
        assertEquals(testParams, capturedParams)
    }

    @Test fun prodParamsAre64MiThreedFourp() {
        assertEquals(65_536, VaultKdf.PROD_PARAMS.mCostKibibytes)
        assertEquals(3, VaultKdf.PROD_PARAMS.tCostIterations)
        assertEquals(4, VaultKdf.PROD_PARAMS.parallelism)
    }

    @Test fun kdfParamsEquality() {
        assertEquals(KdfParams(64, 1, 1), KdfParams(64, 1, 1))
        assertTrue(KdfParams(64, 1, 1) != KdfParams(64, 2, 1))
    }
}
