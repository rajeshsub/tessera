package io.github.rajeshsub.tessera.core.crypto

object VaultMigrator {
    const val CURRENT_VERSION: Int = 1

    data class VaultData(
        val header: VaultHeader,
        val ciphertext: ByteArray,
    )

    fun needsMigration(version: Int): Boolean = version < CURRENT_VERSION

    /** Migrates [data] from [version] to [CURRENT_VERSION]. No-op while only v1 exists. */
    fun migrate(
        version: Int,
        data: VaultData,
    ): Pair<Int, VaultData> {
        require(version in 1..CURRENT_VERSION) { "Unknown vault version: $version" }
        return CURRENT_VERSION to data
    }
}
