package io.github.rajeshsub.tessera.core.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.nio.CharBuffer
import java.util.Arrays

internal fun interface ArgonHasher {
    fun hash(
        password: ByteArray,
        salt: ByteArray,
        params: KdfParams,
    ): ByteArray
}

object VaultKdf {
    val PROD_PARAMS = KdfParams(mCostKibibytes = 65_536, tCostIterations = 3, parallelism = 4)

    fun derive(
        password: CharArray,
        salt: ByteArray,
        params: KdfParams = PROD_PARAMS,
    ): ByteArray = deriveInternal(password, salt, params, Argon2KtHasher)

    internal fun deriveInternal(
        password: CharArray,
        salt: ByteArray,
        params: KdfParams,
        hasher: ArgonHasher,
    ): ByteArray {
        val passwordBytes = charArrayToUtf8(password)
        return try {
            hasher.hash(passwordBytes, salt, params)
        } finally {
            Arrays.fill(passwordBytes, 0)
        }
    }

    private fun charArrayToUtf8(chars: CharArray): ByteArray {
        val charBuffer = CharBuffer.wrap(chars)
        val byteBuffer = Charsets.UTF_8.encode(charBuffer)
        val bytes = ByteArray(byteBuffer.limit())
        byteBuffer.get(bytes)
        Arrays.fill(byteBuffer.array(), 0)
        return bytes
    }
}

private object Argon2KtHasher : ArgonHasher {
    override fun hash(
        password: ByteArray,
        salt: ByteArray,
        params: KdfParams,
    ): ByteArray {
        val argon2Kt = Argon2Kt()
        val result =
            argon2Kt.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = password,
                salt = salt,
                tCostInIterations = params.tCostIterations,
                mCostInKibibyte = params.mCostKibibytes,
                parallelism = params.parallelism,
                hashLengthInBytes = 32,
            )
        return result.rawHashAsByteArray()
    }
}
