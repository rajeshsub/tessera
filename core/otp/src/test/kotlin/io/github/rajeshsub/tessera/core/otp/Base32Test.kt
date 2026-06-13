package io.github.rajeshsub.tessera.core.otp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Base32Test {
    // RFC 4648 Section 10 decode vectors (reverse of encode table)
    @Test fun decodeEmpty() = assertArrayEquals(byteArrayOf(), Base32.decode(""))

    @Test fun decodePadded1() = assertArrayEquals("f".toByteArray(), Base32.decode("MY======"))

    @Test fun decodePadded2() = assertArrayEquals("fo".toByteArray(), Base32.decode("MZXQ===="))

    @Test fun decodePadded3() = assertArrayEquals("foo".toByteArray(), Base32.decode("MZXW6==="))

    @Test fun decodePadded4() = assertArrayEquals("foob".toByteArray(), Base32.decode("MZXW6YQ="))

    @Test fun decodeFull() = assertArrayEquals("foobar".toByteArray(), Base32.decode("MZXW6YTBOI======"))

    // Case-insensitive
    @Test fun decodeLower() = assertArrayEquals("foo".toByteArray(), Base32.decode("mzxw6==="))

    // No padding (common in otpauth URIs)
    @Test fun decodeNoPadding() = assertArrayEquals("foo".toByteArray(), Base32.decode("MZXW6"))

    // Spaces stripped (some services include spaces for readability)
    @Test fun decodeWithSpaces() = assertArrayEquals("foo".toByteArray(), Base32.decode("MZX W6"))

    // Known TOTP secret from Google Authenticator QR code "JBSWY3DPEHPK3PXP"
    @Test fun decodeGoogleSecret() {
        val expected =
            byteArrayOf(
                0x48,
                0x65,
                0x6C,
                0x6C,
                0x6F,
                0x21,
                0xDE.toByte(),
                0xAD.toByte(),
                0xBE.toByte(),
                0xEF.toByte(),
            )
        assertArrayEquals(expected, Base32.decode("JBSWY3DPEHPK3PXP"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectInvalidChar() {
        Base32.decode("AAAA1AAA")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectNonAsciiChar() {
        Base32.decode("AAAAéAAA")
    } // 'é' has code 233 > 127

    @Test fun encodeEmpty() = assertEquals("", Base32.encode(byteArrayOf()))

    @Test fun encode1() = assertEquals("MY======", Base32.encode("f".toByteArray()))

    @Test fun encode3() = assertEquals("MZXW6===", Base32.encode("foo".toByteArray()))

    @Test fun encodeFull() = assertEquals("MZXW6YTBOI======", Base32.encode("foobar".toByteArray()))
}
