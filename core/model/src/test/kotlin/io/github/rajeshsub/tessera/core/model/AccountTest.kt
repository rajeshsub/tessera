package io.github.rajeshsub.tessera.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountTest {
    private fun make(
        id: String = "a1",
        secret: ByteArray = byteArrayOf(1, 2, 3),
    ) = Account(
        id = id,
        type = OtpType.TOTP,
        label = "alice@example.com",
        issuer = "Example",
        secret = secret,
    )

    @Test fun defaultFieldValues() {
        val a = make()
        assertEquals(OtpAlgorithm.SHA1, a.algorithm)
        assertEquals(6, a.digits)
        assertEquals(30, a.period)
        assertEquals(0L, a.counter)
    }

    @Test fun equalsWhenAllFieldsSame() {
        assertEquals(make(), make())
    }

    @Test fun notEqualsWhenSecretDiffers() {
        assertFalse(make(secret = byteArrayOf(1, 2, 3)) == make(secret = byteArrayOf(1, 2, 4)))
    }

    @Test fun notEqualsWhenIdDiffers() {
        assertFalse(make("a1") == make("a2"))
    }

    @Test fun hashCodeConsistentWithEquals() {
        assertEquals(make().hashCode(), make().hashCode())
    }

    @Test fun hashCodeDiffersForDifferentSecrets() {
        assertNotEquals(
            make(secret = byteArrayOf(1, 2, 3)).hashCode(),
            make(secret = byteArrayOf(4, 5, 6)).hashCode(),
        )
    }

    @Test fun toStringContainsLabel() {
        assertTrue(make().toString().contains("alice@example.com"))
    }

    @Test fun toStringDoesNotContainRawSecretReference() {
        // ByteArray.toString() produces "[B@..." — that must not appear
        assertFalse(make().toString().contains("[B@"))
    }

    @Test fun copyPreservesValues() {
        val original = make()
        val copy = original.copy(label = "bob@example.com")
        assertEquals("bob@example.com", copy.label)
        assertEquals(original.id, copy.id)
        assertTrue(original.secret.contentEquals(copy.secret))
    }

    @Test fun nullIssuerSupported() {
        val a = make().copy(issuer = null)
        assertEquals(a, a.copy())
    }
}
