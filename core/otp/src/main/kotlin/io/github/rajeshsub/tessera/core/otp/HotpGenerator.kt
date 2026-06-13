package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm

object HotpGenerator {
    fun generate(
        secret: ByteArray,
        counter: Long,
        digits: Int = 6,
        algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
    ): String = HmacOtp.compute(secret, counter, algorithm, digits)
}
