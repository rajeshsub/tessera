package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object HmacOtp {
    fun compute(
        secret: ByteArray,
        counter: Long,
        algorithm: OtpAlgorithm,
        digits: Int,
    ): String {
        val mac = Mac.getInstance(algorithm.jcaName)
        mac.init(SecretKeySpec(secret, algorithm.jcaName))

        val msg = ByteArray(8)
        var c = counter
        for (i in 7 downTo 0) {
            msg[i] = (c and 0xFF).toByte()
            c = c ushr 8
        }

        val hash = mac.doFinal(msg)

        // Dynamic truncation (RFC 4226 Section 5.3)
        val offset = hash.last().toInt() and 0x0F
        val truncated =
            ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val code = truncated % POWERS[digits]
        return code.toString().padStart(digits, '0')
    }

    private val POWERS = IntArray(9) { d -> Math.pow(10.0, d.toDouble()).toInt() }
}

private val OtpAlgorithm.jcaName: String
    get() =
        when (this) {
            OtpAlgorithm.SHA1 -> "HmacSHA1"
            OtpAlgorithm.SHA256 -> "HmacSHA256"
            OtpAlgorithm.SHA512 -> "HmacSHA512"
        }
