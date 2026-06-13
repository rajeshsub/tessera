package io.github.rajeshsub.tessera.data.vault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Suppress("TooManyFunctions")
open class BiometricKeystore(
    private val encFile: File,
) {
    private companion object {
        const val KEY_ALIAS = "tessera_biometric_key"
        const val PROVIDER = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
        const val KEY_SIZE_BITS = 256
    }

    fun generateKey() {
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        kg.init(
            KeyGenParameterSpec
                .Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build(),
        )
        kg.generateKey()
    }

    open fun isKeyAvailable(): Boolean =
        runCatching {
            val ks = KeyStore.getInstance(PROVIDER).apply { load(null) }
            ks.containsAlias(KEY_ALIAS)
        }.getOrDefault(false)

    open fun isEnrolled(): Boolean = isKeyAvailable() && encFile.exists()

    fun getEncryptCipher(): Cipher {
        val key = loadKey()
        return Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    fun getDecryptCipher(): Cipher {
        val data = encFile.readBytes()
        val ivLen = data[0].toInt() and 0xFF
        val iv = data.copyOfRange(1, 1 + ivLen)
        val key = loadKey()
        return Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
    }

    fun saveEncryptedPassword(
        cipher: Cipher,
        password: CharArray,
    ) {
        val passwordBytes = Charsets.UTF_8.encode(CharBuffer.wrap(password)).array()
        val ciphertext = cipher.doFinal(passwordBytes)
        val iv = cipher.iv
        val tmp = File(encFile.parentFile, "${encFile.name}.tmp")
        tmp.writeBytes(byteArrayOf(iv.size.toByte()) + iv + ciphertext)
        if (!tmp.renameTo(encFile)) {
            encFile.delete()
            tmp.renameTo(encFile)
        }
        passwordBytes.fill(0)
    }

    fun decryptPassword(cipher: Cipher): CharArray {
        val data = encFile.readBytes()
        val ivLen = data[0].toInt() and 0xFF
        val ciphertext = data.copyOfRange(1 + ivLen, data.size)
        val plaintext = cipher.doFinal(ciphertext)
        val chars = Charsets.UTF_8.decode(ByteBuffer.wrap(plaintext)).array()
        plaintext.fill(0)
        return chars
    }

    fun deleteEnrollment() {
        runCatching {
            val ks = KeyStore.getInstance(PROVIDER).apply { load(null) }
            ks.deleteEntry(KEY_ALIAS)
        }
        encFile.delete()
    }

    private fun loadKey(): SecretKey {
        val ks = KeyStore.getInstance(PROVIDER).apply { load(null) }
        return ks.getKey(KEY_ALIAS, null) as SecretKey
    }
}
