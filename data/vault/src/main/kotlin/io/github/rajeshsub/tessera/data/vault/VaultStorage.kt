package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.crypto.AesGcmCipher
import io.github.rajeshsub.tessera.core.crypto.KdfParams
import io.github.rajeshsub.tessera.core.crypto.VaultFormat
import io.github.rajeshsub.tessera.core.crypto.VaultHeader
import io.github.rajeshsub.tessera.core.model.Account
import java.io.File
import java.security.SecureRandom

internal class VaultStorage(
    private val file: File,
) {
    fun exists(): Boolean = file.exists()

    fun readHeader(): VaultHeader {
        val (_, header, _) = VaultFormat.decode(file.readBytes())
        return header
    }

    fun readAndDecryptAccounts(key: ByteArray): List<Account> {
        val (_, header, ciphertext) = VaultFormat.decode(file.readBytes())
        val plaintext = AesGcmCipher.decrypt(key, header.iv, ciphertext)
        return AccountSerializer.decode(plaintext)
    }

    fun saveAccounts(
        key: ByteArray,
        salt: ByteArray,
        params: KdfParams,
        accounts: List<Account>,
    ) {
        val iv = ByteArray(AesGcmCipher.IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val header = VaultHeader(params, salt, iv)
        val plaintext = AccountSerializer.encode(accounts)
        val ciphertext = AesGcmCipher.encrypt(key, iv, plaintext)
        writeAtomic(VaultFormat.encode(header, ciphertext))
    }

    private fun writeAtomic(bytes: ByteArray) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeBytes(bytes)
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }
}
