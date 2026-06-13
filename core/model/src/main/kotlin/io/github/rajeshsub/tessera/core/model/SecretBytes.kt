package io.github.rajeshsub.tessera.core.model

/** Wraps a sensitive byte array. Zeros the array when [use] or [close] completes. */
class SecretBytes private constructor(
    private val bytes: ByteArray,
) : AutoCloseable {
    val size: Int get() = bytes.size

    fun <T> use(block: (ByteArray) -> T): T =
        try {
            block(bytes)
        } finally {
            bytes.fill(0)
        }

    override fun close() {
        bytes.fill(0)
    }

    companion object {
        /** Takes ownership of [bytes]; caller must not use the array after this call. */
        fun wrapExact(bytes: ByteArray): SecretBytes = SecretBytes(bytes)

        /** Copies [bytes] so the caller retains the original. */
        fun wrap(bytes: ByteArray): SecretBytes = SecretBytes(bytes.copyOf())
    }
}
