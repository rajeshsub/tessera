package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpAuthUriExporterTest {
    private val totp =
        Account(
            id = "a1",
            type = OtpType.TOTP,
            label = "alice@example.com",
            issuer = "Example",
            secret = byteArrayOf(72, 101, 108, 108, 111, 33),
        )

    @Test fun totpScheme() {
        assertTrue(OtpAuthUriExporter.build(totp).startsWith("otpauth://totp/"))
    }

    @Test fun containsSecretParam() {
        assertTrue(OtpAuthUriExporter.build(totp).contains("secret="))
    }

    @Test fun secretIsBase32Alphabet() {
        val uri = OtpAuthUriExporter.build(totp)
        val secret = uri.substringAfter("secret=").substringBefore("&")
        assertTrue(secret.all { it in 'A'..'Z' || it in '2'..'7' })
    }

    @Test fun labelEncodesIssuerPrefix() {
        val uri = OtpAuthUriExporter.build(totp)
        assertTrue(uri.contains("Example"))
    }

    @Test fun issuerParamPresent() {
        assertTrue(OtpAuthUriExporter.build(totp).contains("issuer=Example"))
    }

    @Test fun defaultAlgorithmOmitted() {
        assertFalse(OtpAuthUriExporter.build(totp).contains("algorithm="))
    }

    @Test fun nonDefaultAlgorithmIncluded() {
        assertTrue(
            OtpAuthUriExporter
                .build(totp.copy(algorithm = OtpAlgorithm.SHA256))
                .contains("algorithm=SHA256"),
        )
    }

    @Test fun defaultDigitsOmitted() {
        assertFalse(OtpAuthUriExporter.build(totp).contains("digits="))
    }

    @Test fun nonDefaultDigitsIncluded() {
        assertTrue(OtpAuthUriExporter.build(totp.copy(digits = 8)).contains("digits=8"))
    }

    @Test fun defaultPeriodOmitted() {
        assertFalse(OtpAuthUriExporter.build(totp).contains("period="))
    }

    @Test fun nonDefaultPeriodIncluded() {
        assertTrue(OtpAuthUriExporter.build(totp.copy(period = 60)).contains("period=60"))
    }

    @Test fun hotpScheme() {
        val hotp = Account(id = "h1", type = OtpType.HOTP, label = "bob", issuer = null, secret = byteArrayOf(1, 2, 3))
        assertTrue(OtpAuthUriExporter.build(hotp).startsWith("otpauth://hotp/"))
    }

    @Test fun hotpIncludesCounter() {
        val hotp =
            Account(
                id = "h1",
                type = OtpType.HOTP,
                label = "bob",
                issuer = null,
                secret = byteArrayOf(1, 2, 3),
                counter = 42L,
            )
        assertTrue(OtpAuthUriExporter.build(hotp).contains("counter=42"))
    }

    @Test fun hotpPeriodOmitted() {
        val hotp =
            Account(id = "h1", type = OtpType.HOTP, label = "bob", issuer = null, secret = byteArrayOf(1), period = 60)
        assertFalse(OtpAuthUriExporter.build(hotp).contains("period="))
    }

    @Test fun nullIssuerOmitsIssuerParam() {
        val account = totp.copy(issuer = null)
        assertFalse(OtpAuthUriExporter.build(account).contains("issuer="))
    }

    @Test fun nullIssuerNoColonInLabel() {
        val account = totp.copy(issuer = null, label = "alice@example.com")
        val uri = OtpAuthUriExporter.build(account)
        val label = uri.substringAfter("otpauth://totp/").substringBefore("?")
        assertFalse(label.contains("%3A") || label.contains(":"))
    }
}
