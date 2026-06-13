package io.github.rajeshsub.tessera.core.crypto

/** Vault header: KDF params + Argon2 salt + AES-GCM IV. */
data class VaultHeader(
    val params: KdfParams,
    val salt: ByteArray, // 16 bytes
    val iv: ByteArray, // 12 bytes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultHeader) return false
        return params == other.params &&
            salt.contentEquals(other.salt) &&
            iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var h = params.hashCode()
        h = 31 * h + salt.contentHashCode()
        h = 31 * h + iv.contentHashCode()
        return h
    }
}
