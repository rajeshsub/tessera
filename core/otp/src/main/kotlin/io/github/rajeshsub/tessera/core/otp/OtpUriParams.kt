package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType

/** Parsed parameters from an `otpauth://` URI. */
data class OtpUriParams(
    val type: OtpType,
    val label: String,
    val secret: ByteArray,
    val issuer: String?,
    val algorithm: OtpAlgorithm,
    val digits: Int,
    val period: Int,
    val counter: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OtpUriParams) return false
        return type == other.type &&
            label == other.label &&
            secret.contentEquals(other.secret) &&
            issuer == other.issuer &&
            algorithm == other.algorithm &&
            digits == other.digits &&
            period == other.period &&
            counter == other.counter
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + digits
        result = 31 * result + period
        result = 31 * result + counter.hashCode()
        return result
    }
}
