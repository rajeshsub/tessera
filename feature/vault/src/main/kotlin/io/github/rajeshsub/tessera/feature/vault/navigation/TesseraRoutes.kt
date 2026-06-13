package io.github.rajeshsub.tessera.feature.vault.navigation

import kotlinx.serialization.Serializable

@Serializable
object Unlock

@Serializable
object AccountList

@Serializable
data class AddAccount(
    val prefillUri: String = "",
)

@Serializable
object QrScan

@Serializable
data class EditAccount(
    val accountId: String,
)

@Serializable
object Settings

@Serializable
object ExportWarning

@Serializable
object BackupRestore
