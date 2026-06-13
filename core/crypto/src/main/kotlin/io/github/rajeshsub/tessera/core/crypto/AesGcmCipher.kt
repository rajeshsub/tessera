package io.github.rajeshsub.tessera.core.crypto

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesGcmCipher {
    const val KEY_LENGTH = 32
    const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    fun encrypt(
        key: ByteArray,
        iv: ByteArray,
        plaintext: ByteArray,
    ): ByteArray {
        require(key.size == KEY_LENGTH) { "AES-256 key must be $KEY_LENGTH bytes, got ${key.size}" }
        require(iv.size == IV_LENGTH) { "GCM IV must be $IV_LENGTH bytes, got ${iv.size}" }
        return cipher(Cipher.ENCRYPT_MODE, key, iv).doFinal(plaintext)
    }

    fun decrypt(
        key: ByteArray,
        iv: ByteArray,
        ciphertext: ByteArray,
    ): ByteArray {
        require(key.size == KEY_LENGTH) { "AES-256 key must be $KEY_LENGTH bytes, got ${key.size}" }
        require(iv.size == IV_LENGTH) { "GCM IV must be $IV_LENGTH bytes, got ${iv.size}" }
        return cipher(Cipher.DECRYPT_MODE, key, iv).doFinal(ciphertext)
    }

    private fun cipher(
        mode: Int,
        key: ByteArray,
        iv: ByteArray,
    ): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").also {
            it.init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
}
