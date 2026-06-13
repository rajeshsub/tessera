package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm

object TotpGenerator {
    private const val T0 = 0L

    fun generate(
        secret: ByteArray,
        epochSeconds: Long,
        digits: Int = 6,
        period: Int = 30,
        algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
    ): String {
        val counter = (epochSeconds - T0) / period
        return HmacOtp.compute(secret, counter, algorithm, digits)
    }

    /** Remaining seconds in the current TOTP window. */
    fun secondsRemaining(
        epochSeconds: Long,
        period: Int = 30,
    ): Int = (period - ((epochSeconds % period).toInt()))
}
