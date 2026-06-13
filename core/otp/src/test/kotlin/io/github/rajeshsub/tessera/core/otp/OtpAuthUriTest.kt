package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtpAuthUriTest {
    // "Hello!..." secret: JBSWY3DPEHPK3PXP
    private val expectedSecret = Base32.decode("JBSWY3DPEHPK3PXP")

    @Test fun parseTotpMinimal() {
        val p = OtpAuthUri.parse("otpauth://totp/alice@example.com?secret=JBSWY3DPEHPK3PXP")!!
        assertEquals(OtpType.TOTP, p.type)
        assertArrayEquals(expectedSecret, p.secret)
        assertEquals("alice@example.com", p.label)
        assertNull(p.issuer)
        assertEquals(6, p.digits)
        assertEquals(30, p.period)
        assertEquals(OtpAlgorithm.SHA1, p.algorithm)
    }

    @Test fun parseTotpFull() {
        val uri =
            "otpauth://totp/Example%3Aalice%40example.com" +
                "?secret=JBSWY3DPEHPK3PXP&issuer=Example&algorithm=SHA256&digits=8&period=60"
        val p = OtpAuthUri.parse(uri)!!
        assertEquals(OtpType.TOTP, p.type)
        assertEquals("Example:alice@example.com", p.label)
        assertEquals("Example", p.issuer)
        assertEquals(OtpAlgorithm.SHA256, p.algorithm)
        assertEquals(8, p.digits)
        assertEquals(60, p.period)
    }

    @Test fun parseHotp() {
        val p = OtpAuthUri.parse("otpauth://hotp/bob?secret=JBSWY3DPEHPK3PXP&counter=42")!!
        assertEquals(OtpType.HOTP, p.type)
        assertEquals(42L, p.counter)
    }

    @Test fun parseIssuerPrefix() {
        // Label may be "Issuer:account" -- issuer extracted from prefix if not in query param
        val p = OtpAuthUri.parse("otpauth://totp/Acme%3Ajane?secret=JBSWY3DPEHPK3PXP")!!
        assertEquals("Acme:jane", p.label)
        assertEquals("Acme", p.issuer)
    }

    @Test fun caseInsensitiveAlgorithm() {
        val p = OtpAuthUri.parse("otpauth://totp/x?secret=JBSWY3DPEHPK3PXP&algorithm=sha512")!!
        assertEquals(OtpAlgorithm.SHA512, p.algorithm)
    }

    @Test fun returnsNullOnBadScheme() = assertNull(OtpAuthUri.parse("https://example.com"))

    @Test fun returnsNullOnMissingSecret() = assertNull(OtpAuthUri.parse("otpauth://totp/alice"))

    @Test fun returnsNullOnInvalidSecret() = assertNull(OtpAuthUri.parse("otpauth://totp/alice?secret=!!!INVALID"))

    @Test fun returnsNullOnNoSlashAfterType() = assertNull(OtpAuthUri.parse("otpauth://totpnoslash"))

    @Test fun returnsNullOnQueryParamWithoutEquals() =
        assertNull(OtpAuthUri.parse("otpauth://totp/alice?secretJBSWY3DPEHPK3PXP"))

    @Test fun returnsNullOnInvalidPercentEncoding() =
        assertNull(OtpAuthUri.parse("otpauth://totp/%ZZ?secret=JBSWY3DPEHPK3PXP"))

    @Test fun returnsNullOnUnknownType() = assertNull(OtpAuthUri.parse("otpauth://steam/alice?secret=JBSWY3DPEHPK3PXP"))

    @Test fun fallsBackToDefaultsOnInvalidNumbers() {
        // digits/period/counter with non-numeric strings -> toIntOrNull/toLongOrNull returns null -> defaults used
        val p =
            OtpAuthUri.parse(
                "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&digits=abc&period=xyz&counter=notanumber",
            )!!
        assertEquals(6, p.digits)
        assertEquals(30, p.period)
        assertEquals(0L, p.counter)
    }
}
