package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.core.otp.Base32
import java.net.URLEncoder

object OtpAuthUriExporter {
    fun build(account: Account): String {
        val typeStr = if (account.type == OtpType.TOTP) "totp" else "hotp"
        val rawLabel = account.issuer?.let { "$it:${account.label}" } ?: account.label
        val encodedLabel = URLEncoder.encode(rawLabel, "UTF-8")
        val secret = Base32.encode(account.secret).trimEnd('=')

        return buildString {
            append("otpauth://").append(typeStr).append('/').append(encodedLabel)
            append("?secret=").append(secret)
            account.issuer?.let { append("&issuer=").append(URLEncoder.encode(it, "UTF-8")) }
            if (account.algorithm != OtpAlgorithm.SHA1) {
                append("&algorithm=").append(account.algorithm.name)
            }
            if (account.digits != 6) append("&digits=").append(account.digits)
            if (account.type == OtpType.TOTP && account.period != 30) {
                append("&period=").append(account.period)
            }
            if (account.type == OtpType.HOTP) append("&counter=").append(account.counter)
        }
    }
}
