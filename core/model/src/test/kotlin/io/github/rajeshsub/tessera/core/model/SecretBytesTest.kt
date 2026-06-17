package io.github.rajeshsub.tessera.core.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretBytesTest {
    @Test fun sizeReflectsWrappedArray() {
        val s = SecretBytes.wrapExact(byteArrayOf(1, 2, 3, 4))
        assertEquals(4, s.size)
    }

    @Test fun useExposesContentsAndZerosAfter() {
        val raw = byteArrayOf(10, 20, 30)
        val s = SecretBytes.wrapExact(raw)
        var seen = ByteArray(0)
        s.use { bytes -> seen = bytes.copyOf() }
        assertArrayEquals(byteArrayOf(10, 20, 30), seen)
        assertArrayEquals(byteArrayOf(0, 0, 0), raw) // original array zeroed
    }

    @Test fun closeZerosBytes() {
        val raw = byteArrayOf(1, 2, 3)
        val s = SecretBytes.wrapExact(raw)
        s.close()
        assertTrue(raw.all { it == 0.toByte() })
    }

    @Test fun wrapCopiesArray() {
        val original = byteArrayOf(7, 8, 9)
        val s = SecretBytes.wrap(original)
        s.close()
        // Original must NOT be zeroed - wrap made a copy
        assertArrayEquals(byteArrayOf(7, 8, 9), original)
    }

    @Test fun usePropagatesReturnValue() {
        val s = SecretBytes.wrapExact(byteArrayOf(42))
        val result = s.use { bytes -> bytes[0].toInt() }
        assertEquals(42, result)
    }

    @Test fun useZerosEvenOnException() {
        val raw = byteArrayOf(5, 6, 7)
        val s = SecretBytes.wrapExact(raw)
        @Suppress("TooGenericExceptionThrown")
        runCatching { s.use { throw RuntimeException("boom") } }
        assertTrue(raw.all { it == 0.toByte() })
    }
}
