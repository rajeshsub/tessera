package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.crypto.VaultFormat
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SafBackupManager(
    private val vaultFile: File,
) {
    fun backup(outputStream: OutputStream) {
        outputStream.write(vaultFile.readBytes())
    }

    fun restore(inputStream: InputStream): Boolean {
        val bytes = inputStream.readBytes()
        VaultFormat.decode(bytes)
        val tmp = File(vaultFile.parentFile, "restore.tmp")
        tmp.writeBytes(bytes)
        return if (!tmp.renameTo(vaultFile)) {
            vaultFile.delete()
            tmp.renameTo(vaultFile)
        } else {
            true
        }
    }
}
