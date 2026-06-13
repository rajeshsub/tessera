package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * RFC 6238 Section 4 test vectors. All codes are 8 digits, period = 30s.
 * Secrets differ per algorithm per the RFC appendix.
 */
class TotpRfc6238Test {
    private val sha1Secret = "12345678901234567890".toByteArray(Charsets.US_ASCII)
    private val sha256Secret = "12345678901234567890123456789012".toByteArray(Charsets.US_ASCII)
    private val sha512Secret =
        "1234567890123456789012345678901234567890123456789012345678901234"
            .toByteArray(Charsets.US_ASCII)

    @Test fun t59_sha1() = eq("94287082", sha1Secret, 59L, OtpAlgorithm.SHA1)

    @Test fun t59_sha256() = eq("46119246", sha256Secret, 59L, OtpAlgorithm.SHA256)

    @Test fun t59_sha512() = eq("90693936", sha512Secret, 59L, OtpAlgorithm.SHA512)

    @Test fun t1111111109_sha1() = eq("07081804", sha1Secret, 1111111109L, OtpAlgorithm.SHA1)

    @Test fun t1111111109_sha256() = eq("68084774", sha256Secret, 1111111109L, OtpAlgorithm.SHA256)

    @Test fun t1111111109_sha512() = eq("25091201", sha512Secret, 1111111109L, OtpAlgorithm.SHA512)

    @Test fun t1111111111_sha1() = eq("14050471", sha1Secret, 1111111111L, OtpAlgorithm.SHA1)

    @Test fun t1111111111_sha256() = eq("67062674", sha256Secret, 1111111111L, OtpAlgorithm.SHA256)

    @Test fun t1111111111_sha512() = eq("99943326", sha512Secret, 1111111111L, OtpAlgorithm.SHA512)

    @Test fun t1234567890_sha1() = eq("89005924", sha1Secret, 1234567890L, OtpAlgorithm.SHA1)

    @Test fun t1234567890_sha256() = eq("91819424", sha256Secret, 1234567890L, OtpAlgorithm.SHA256)

    @Test fun t1234567890_sha512() = eq("93441116", sha512Secret, 1234567890L, OtpAlgorithm.SHA512)

    @Test fun t2000000000_sha1() = eq("69279037", sha1Secret, 2000000000L, OtpAlgorithm.SHA1)

    @Test fun t2000000000_sha256() = eq("90698825", sha256Secret, 2000000000L, OtpAlgorithm.SHA256)

    @Test fun t2000000000_sha512() = eq("38618901", sha512Secret, 2000000000L, OtpAlgorithm.SHA512)

    @Test fun t20000000000_sha1() = eq("65353130", sha1Secret, 20000000000L, OtpAlgorithm.SHA1)

    @Test fun t20000000000_sha256() = eq("77737706", sha256Secret, 20000000000L, OtpAlgorithm.SHA256)

    @Test fun t20000000000_sha512() = eq("47863826", sha512Secret, 20000000000L, OtpAlgorithm.SHA512)

    private fun eq(
        expected: String,
        secret: ByteArray,
        epochSeconds: Long,
        alg: OtpAlgorithm,
    ) = assertEquals(expected, TotpGenerator.generate(secret, epochSeconds, digits = 8, algorithm = alg))
}
