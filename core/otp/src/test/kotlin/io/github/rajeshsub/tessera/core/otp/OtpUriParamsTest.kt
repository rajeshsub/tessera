package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpUriParamsTest {
    private val secret = Base32.decode("JBSWY3DPEHPK3PXP")

    @Suppress("LongParameterList")
    private fun params(
        type: OtpType = OtpType.TOTP,
        label: String = "alice",
        secret: ByteArray = this.secret,
        issuer: String? = "Example",
        algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
        digits: Int = 6,
        period: Int = 30,
        counter: Long = 0L,
    ) = OtpUriParams(type, label, secret, issuer, algorithm, digits, period, counter)

    @Test fun equalsSameContent() {
        assertEquals(params(), params())
    }

    @Test fun equalsSameRef() {
        val p = params()
        assertTrue(p == p)
    }

    @Test fun notEqualsOtherType() {
        assertNotEquals(params(type = OtpType.TOTP), params(type = OtpType.HOTP))
    }

    @Test fun notEqualsOtherSecret() {
        assertNotEquals(params(), params(secret = byteArrayOf(1, 2, 3)))
    }

    @Test fun notEqualsOtherClass() {
        assertFalse(params().equals("not a params"))
    }

    @Test fun hashCodeConsistent() {
        assertEquals(params().hashCode(), params().hashCode())
    }

    @Test fun hashCodeDiffersOnSecretChange() {
        assertNotEquals(params().hashCode(), params(secret = byteArrayOf(0)).hashCode())
    }

    @Test fun hashCodeNullIssuer() {
        val p = params(issuer = null)
        assertEquals(p.hashCode(), p.hashCode())
    }

    @Test fun copyLabel() {
        val p = params()
        val copy = p.copy(label = "bob")
        assertEquals("bob", copy.label)
        assertEquals(p.type, copy.type)
    }

    @Test fun componentFunctionsAndProperties() {
        val p = params()
        assertEquals(OtpType.TOTP, p.type)
        assertEquals("alice", p.label)
        assertTrue(secret.contentEquals(p.secret))
        assertEquals("Example", p.issuer)
        assertEquals(OtpAlgorithm.SHA1, p.algorithm)
        assertEquals(6, p.digits)
        assertEquals(30, p.period)
        assertEquals(0L, p.counter)
    }

    @Test fun toStringContainsLabel() {
        assertTrue(params().toString().contains("alice"))
    }
}
