package io.github.rajeshsub.tessera.core.otp

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val DECODE_TABLE =
        IntArray(128) { -1 }.also { t ->
            ALPHABET.forEachIndexed { i, c -> t[c.code] = i }
            // lowercase
            ('a'..'z').forEachIndexed { i, c -> t[c.code] = i }
            "234567".forEachIndexed { i, c -> t[c.code] = 26 + i }
        }

    fun decode(input: String): ByteArray {
        val cleaned = input.replace(" ", "").uppercase().trimEnd('=')
        if (cleaned.isEmpty()) return byteArrayOf()

        val out = ByteArray(cleaned.length * 5 / 8)
        var buffer = 0
        var bitsLeft = 0
        var outIdx = 0

        for (ch in cleaned) {
            val code = if (ch.code < 128) DECODE_TABLE[ch.code] else -1
            require(code != -1) { "Invalid Base32 character: '$ch'" }
            buffer = (buffer shl 5) or code
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out[outIdx++] = (buffer shr bitsLeft).toByte()
                buffer = buffer and ((1 shl bitsLeft) - 1)
            }
        }
        return out
    }

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in input) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                bitsLeft -= 5
                sb.append(ALPHABET[(buffer shr bitsLeft) and 0x1F])
            }
        }
        if (bitsLeft > 0) {
            sb.append(ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        // Pad to multiple of 8
        while (sb.length % 8 != 0) sb.append('=')
        return sb.toString()
    }
}
