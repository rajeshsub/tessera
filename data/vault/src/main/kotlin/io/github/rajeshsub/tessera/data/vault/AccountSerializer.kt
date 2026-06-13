package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.Base32
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

internal object AccountSerializer {
    fun encode(accounts: List<Account>): ByteArray {
        val entities =
            accounts.map { account ->
                AccountEntity(
                    id = account.id,
                    type = account.type.name,
                    label = account.label,
                    issuer = account.issuer,
                    secret = Base32.encode(account.secret),
                    algorithm = account.algorithm.name,
                    digits = account.digits,
                    period = account.period,
                    counter = account.counter,
                )
            }
        return json.encodeToString(entities).toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): List<Account> {
        val entities: List<AccountEntity> = json.decodeFromString(String(bytes, Charsets.UTF_8))
        return entities.map { entity ->
            Account(
                id = entity.id,
                type = OtpType.valueOf(entity.type),
                label = entity.label,
                issuer = entity.issuer,
                secret = Base32.decode(entity.secret),
                algorithm = OtpAlgorithm.valueOf(entity.algorithm),
                digits = entity.digits,
                period = entity.period,
                counter = entity.counter,
            )
        }
    }
}
