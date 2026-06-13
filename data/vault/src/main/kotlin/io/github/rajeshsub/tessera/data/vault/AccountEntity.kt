package io.github.rajeshsub.tessera.data.vault

import kotlinx.serialization.Serializable

@Serializable
internal data class AccountEntity(
    val id: String,
    val type: String,
    val label: String,
    val issuer: String? = null,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0L,
)
