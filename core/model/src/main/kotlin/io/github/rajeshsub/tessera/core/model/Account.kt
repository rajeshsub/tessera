package io.github.rajeshsub.tessera.core.model

@Suppress("LongParameterList")
data class Account(
    val id: String,
    val type: OtpType,
    val label: String,
    val issuer: String?,
    val secret: ByteArray,
    val algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return id == other.id &&
            type == other.type &&
            label == other.label &&
            issuer == other.issuer &&
            secret.contentEquals(other.secret) &&
            algorithm == other.algorithm &&
            digits == other.digits &&
            period == other.period &&
            counter == other.counter
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + digits
        result = 31 * result + period
        result = 31 * result + counter.hashCode()
        return result
    }

    override fun toString(): String =
        "Account(id=$id, type=$type, label=$label, issuer=$issuer, " +
            "algorithm=$algorithm, digits=$digits, period=$period, counter=$counter)"
}
