package io.github.rajeshsub.tessera.core.crypto

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Vault binary format:
 *   [MAGIC: 4 bytes "TESS"] [VERSION: 1 byte] [HEADER_LEN: 2 bytes BE]
 *   [HEADER: HEADER_LEN bytes] [CIPHERTEXT: remaining bytes]
 *
 * Version-1 header (34 bytes):
 *   [M_COST: 4 bytes BE] [T_COST: 1 byte] [PARALLELISM: 1 byte]
 *   [SALT: 16 bytes] [IV: 12 bytes]
 */
object VaultFormat {
    private val MAGIC = byteArrayOf('T'.code.toByte(), 'E'.code.toByte(), 'S'.code.toByte(), 'S'.code.toByte())
    private const val SUPPORTED_VERSION: Byte = 1
    private const val HEADER_V1_LEN = 34 // 4+1+1+16+12

    data class DecodedVault(
        val version: Int,
        val header: VaultHeader,
        val ciphertext: ByteArray,
    )

    fun encode(
        header: VaultHeader,
        ciphertext: ByteArray,
    ): ByteArray {
        val buf =
            ByteBuffer
                .allocate(4 + 1 + 2 + HEADER_V1_LEN + ciphertext.size)
                .order(ByteOrder.BIG_ENDIAN)
        buf.put(MAGIC)
        buf.put(SUPPORTED_VERSION)
        buf.putShort(HEADER_V1_LEN.toShort())
        buf.putInt(header.params.mCostKibibytes)
        buf.put(header.params.tCostIterations.toByte())
        buf.put(header.params.parallelism.toByte())
        buf.put(header.salt)
        buf.put(header.iv)
        buf.put(ciphertext)
        return buf.array()
    }

    fun decode(bytes: ByteArray): DecodedVault {
        require(bytes.size >= 4 + 1 + 2) { "Vault data too short" }
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        val magic = ByteArray(4).also { buf.get(it) }
        require(magic.contentEquals(MAGIC)) { "Invalid vault magic" }

        val version = buf.get()
        require(version == SUPPORTED_VERSION) { "Unsupported vault version: $version" }

        val headerLen = buf.short.toInt() and 0xFFFF
        require(buf.remaining() >= headerLen) { "Truncated vault header" }

        val mCost = buf.int
        val tCost = buf.get().toInt() and 0xFF
        val parallelism = buf.get().toInt() and 0xFF
        val salt = ByteArray(16).also { buf.get(it) }
        val iv = ByteArray(12).also { buf.get(it) }

        val ciphertext = ByteArray(buf.remaining()).also { buf.get(it) }

        return DecodedVault(
            version = version.toInt(),
            header = VaultHeader(KdfParams(mCost, tCost, parallelism), salt, iv),
            ciphertext = ciphertext,
        )
    }
}
