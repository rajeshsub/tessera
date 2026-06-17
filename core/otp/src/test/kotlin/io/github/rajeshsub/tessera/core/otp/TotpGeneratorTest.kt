package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpGeneratorTest {
    private val sha1Secret = "12345678901234567890".toByteArray(Charsets.US_ASCII)

    @Test fun generateAllDefaults() {
        // Calls generate with only required params - exercises default digits=6, period=30, SHA1
        // RFC 6238 t59: counter = 59/30 = 1 -> same as HotpRfc4226Test.counter1 = "287082"
        assertEquals("287082", TotpGenerator.generate(sha1Secret, 59L))
    }

    @Test fun generateDefaultPeriodExplicitAlgorithm() {
        // Exercises default period=30 with explicit algorithm
        assertEquals("287082", TotpGenerator.generate(sha1Secret, 59L, algorithm = OtpAlgorithm.SHA1))
    }

    @Test fun secondsRemainingMidWindow() {
        // epoch 44 -> T = 44/30 = 1, elapsed = 44 % 30 = 14, remaining = 30 - 14 = 16
        assertEquals(16, TotpGenerator.secondsRemaining(44L))
    }

    @Test fun secondsRemainingAtWindowStart() {
        // epoch 60 -> elapsed = 60 % 30 = 0, remaining = 30
        assertEquals(30, TotpGenerator.secondsRemaining(60L))
    }

    @Test fun secondsRemainingAtWindowEnd() {
        // epoch 59 -> elapsed = 59 % 30 = 29, remaining = 1
        assertEquals(1, TotpGenerator.secondsRemaining(59L))
    }

    @Test fun secondsRemainingInRange() {
        val r = TotpGenerator.secondsRemaining(System.currentTimeMillis() / 1000)
        assertTrue(r in 1..30)
    }

    @Test fun customPeriod() {
        // period=60: epoch 100 -> elapsed = 100 % 60 = 40, remaining = 20
        assertEquals(20, TotpGenerator.secondsRemaining(100L, period = 60))
    }
}
