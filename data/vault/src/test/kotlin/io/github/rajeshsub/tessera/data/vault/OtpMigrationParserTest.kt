package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpMigrationParserTest {
    // Proto payload: TOTP / alice@example.com / issuer=Example / secret=Hello!(72,101,108,108,111,33)
    private val totpUrl =
        "otpauth-migration://offline?data=" +
            "CigKBkhlbGxvIRIRYWxpY2VAZXhhbXBsZS5jb20aB0V4YW1wbGUoAjAB"

    // Proto payload: HOTP / bob / secret=Hello! / counter=5
    private val hotpUrl =
        "otpauth-migration://offline?data=ChEKBkhlbGxvIRIDYm9iKAE4BQ%3D%3D"

    @Test fun parsesTotpLabel() {
        assertEquals("alice@example.com", OtpMigrationParser.parse(totpUrl)[0].label)
    }

    @Test fun parsesTotpIssuer() {
        assertEquals("Example", OtpMigrationParser.parse(totpUrl)[0].issuer)
    }

    @Test fun parsesTotpType() {
        assertEquals(OtpType.TOTP, OtpMigrationParser.parse(totpUrl)[0].type)
    }

    @Test fun parsesTotpSecret() {
        assertArrayEquals(
            byteArrayOf(72, 101, 108, 108, 111, 33),
            OtpMigrationParser.parse(totpUrl)[0].secret,
        )
    }

    @Test fun totpDefaultsAlgorithmSha1() {
        assertEquals(OtpAlgorithm.SHA1, OtpMigrationParser.parse(totpUrl)[0].algorithm)
    }

    @Test fun totpDefaultsSixDigits() {
        assertEquals(6, OtpMigrationParser.parse(totpUrl)[0].digits)
    }

    @Test fun totpDefaultPeriod30() {
        assertEquals(30, OtpMigrationParser.parse(totpUrl)[0].period)
    }

    @Test fun totpDefaultCounterZero() {
        assertEquals(0L, OtpMigrationParser.parse(totpUrl)[0].counter)
    }

    @Test fun parsedAccountHasNonBlankId() {
        assertTrue(OtpMigrationParser.parse(totpUrl)[0].id.isNotBlank())
    }

    @Test fun eachParseGetsUniqueId() {
        val a = OtpMigrationParser.parse(totpUrl)[0]
        val b = OtpMigrationParser.parse(totpUrl)[0]
        assertNotEquals(a.id, b.id)
    }

    @Test fun parsesHotpType() {
        assertEquals(OtpType.HOTP, OtpMigrationParser.parse(hotpUrl)[0].type)
    }

    @Test fun parsesHotpLabel() {
        assertEquals("bob", OtpMigrationParser.parse(hotpUrl)[0].label)
    }

    @Test fun parsesHotpNullIssuer() {
        assertNull(OtpMigrationParser.parse(hotpUrl)[0].issuer)
    }

    @Test fun parsesHotpCounter() {
        assertEquals(5L, OtpMigrationParser.parse(hotpUrl)[0].counter)
    }

    @Test fun nonMigrationUrlReturnsEmpty() {
        assertTrue(OtpMigrationParser.parse("otpauth://totp/test?secret=ABC").isEmpty())
    }

    @Test fun emptyStringReturnsEmpty() {
        assertTrue(OtpMigrationParser.parse("").isEmpty())
    }

    @Test fun emptyDataParamReturnsEmpty() {
        assertTrue(OtpMigrationParser.parse("otpauth-migration://offline?data=").isEmpty())
    }

    @Test fun malformedBase64ReturnsEmpty() {
        assertTrue(
            OtpMigrationParser.parse("otpauth-migration://offline?data=!!!bad!!!").isEmpty(),
        )
    }
}
