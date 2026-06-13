package io.github.rajeshsub.tessera.core.otp

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * RFC 4226 Appendix D test vectors.
 * Secret: ASCII "12345678901234567890" (20 bytes), SHA-1, 6 digits.
 */
class HotpRfc4226Test {
    private val secret = "12345678901234567890".toByteArray(Charsets.US_ASCII)

    @Test fun counter0() = assertEquals("755224", HotpGenerator.generate(secret, 0L))

    @Test fun counter1() = assertEquals("287082", HotpGenerator.generate(secret, 1L))

    @Test fun counter2() = assertEquals("359152", HotpGenerator.generate(secret, 2L))

    @Test fun counter3() = assertEquals("969429", HotpGenerator.generate(secret, 3L))

    @Test fun counter4() = assertEquals("338314", HotpGenerator.generate(secret, 4L))

    @Test fun counter5() = assertEquals("254676", HotpGenerator.generate(secret, 5L))

    @Test fun counter6() = assertEquals("287922", HotpGenerator.generate(secret, 6L))

    @Test fun counter7() = assertEquals("162583", HotpGenerator.generate(secret, 7L))

    @Test fun counter8() = assertEquals("399871", HotpGenerator.generate(secret, 8L))

    @Test fun counter9() = assertEquals("520489", HotpGenerator.generate(secret, 9L))

    @Test fun eightDigits() = assertEquals("84755224", HotpGenerator.generate(secret, 0L, digits = 8))
}
