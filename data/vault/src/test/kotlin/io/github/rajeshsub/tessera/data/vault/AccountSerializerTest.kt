package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountSerializerTest {
    private val totp =
        Account(
            id = "abc-123",
            type = OtpType.TOTP,
            label = "alice@example.com",
            issuer = "Example",
            secret = byteArrayOf(72, 101, 108, 108, 111, 33), // "Hello!"
        )

    private val hotp =
        Account(
            id = "def-456",
            type = OtpType.HOTP,
            label = "bob",
            issuer = null,
            secret = byteArrayOf(1, 2, 3),
            algorithm = OtpAlgorithm.SHA256,
            digits = 8,
            counter = 42L,
        )

    @Test fun roundTripSingleAccount() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(listOf(totp)))
        assertEquals(1, decoded.size)
        assertEquals(totp, decoded[0])
    }

    @Test fun roundTripMultipleAccounts() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(listOf(totp, hotp)))
        assertEquals(2, decoded.size)
        assertEquals(totp, decoded[0])
        assertEquals(hotp, decoded[1])
    }

    @Test fun roundTripEmptyList() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(emptyList()))
        assertTrue(decoded.isEmpty())
    }

    @Test fun encodedIsValidUtf8Json() {
        val json = String(AccountSerializer.encode(listOf(totp)), Charsets.UTF_8)
        assertTrue(json.startsWith("["))
        assertTrue(json.contains("abc-123"))
        assertTrue(json.contains("TOTP"))
    }

    @Test fun secretStoredAsBase32Alphabet() {
        val json = String(AccountSerializer.encode(listOf(totp)), Charsets.UTF_8)
        val secretValue = json.substringAfter("\"secret\":\"").substringBefore("\"")
        assertTrue(secretValue.all { it in 'A'..'Z' || it in '2'..'7' || it == '=' })
    }

    @Test fun hotpCounterPreserved() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(listOf(hotp)))
        assertEquals(42L, decoded[0].counter)
    }

    @Test fun algorithmAndDigitsPreserved() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(listOf(hotp)))
        assertEquals(OtpAlgorithm.SHA256, decoded[0].algorithm)
        assertEquals(8, decoded[0].digits)
    }

    @Test fun nullIssuerPreserved() {
        val decoded = AccountSerializer.decode(AccountSerializer.encode(listOf(hotp)))
        assertEquals(null, decoded[0].issuer)
    }
}
