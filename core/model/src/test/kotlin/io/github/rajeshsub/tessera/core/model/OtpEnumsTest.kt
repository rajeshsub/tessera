package io.github.rajeshsub.tessera.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OtpEnumsTest {
    @Test fun otpTypeValues() {
        assertEquals(2, OtpType.entries.size)
        assertNotNull(OtpType.TOTP)
        assertNotNull(OtpType.HOTP)
    }

    @Test fun otpAlgorithmValues() {
        assertEquals(3, OtpAlgorithm.entries.size)
        assertNotNull(OtpAlgorithm.SHA1)
        assertNotNull(OtpAlgorithm.SHA256)
        assertNotNull(OtpAlgorithm.SHA512)
    }

    @Test fun otpTypeValueOf() {
        assertEquals(OtpType.TOTP, OtpType.valueOf("TOTP"))
        assertEquals(OtpType.HOTP, OtpType.valueOf("HOTP"))
    }

    @Test fun otpAlgorithmValueOf() {
        assertEquals(OtpAlgorithm.SHA1, OtpAlgorithm.valueOf("SHA1"))
        assertEquals(OtpAlgorithm.SHA256, OtpAlgorithm.valueOf("SHA256"))
        assertEquals(OtpAlgorithm.SHA512, OtpAlgorithm.valueOf("SHA512"))
    }
}
