package io.github.rajeshsub.tessera.core.otp

import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import java.net.URLDecoder

/**
 * Parses `otpauth://` URIs per the Google Authenticator Key URI Format.
 * Returns null for any invalid or unrecognised input rather than throwing.
 */
object OtpAuthUri {
    fun parse(uri: String): OtpUriParams? = runCatching { doParse(uri) }.getOrNull()

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun doParse(uri: String): OtpUriParams? {
        if (!uri.startsWith("otpauth://")) return null

        val withoutScheme = uri.removePrefix("otpauth://")
        val typeEnd = withoutScheme.indexOf('/')
        if (typeEnd < 0) return null

        val typeStr = withoutScheme.substring(0, typeEnd)
        val type =
            when (typeStr.lowercase()) {
                "totp" -> OtpType.TOTP
                "hotp" -> OtpType.HOTP
                else -> return null
            }

        val rest = withoutScheme.substring(typeEnd + 1)
        val queryStart = rest.indexOf('?')
        val rawLabel = if (queryStart >= 0) rest.substring(0, queryStart) else rest
        val label = decode(rawLabel)

        val queryString = if (queryStart >= 0) rest.substring(queryStart + 1) else ""
        val params =
            queryString
                .split('&')
                .mapNotNull {
                    val eq = it.indexOf('=')
                    if (eq < 0) null else it.substring(0, eq).lowercase() to decode(it.substring(eq + 1))
                }.toMap()

        val secretStr = params["secret"] ?: return null
        val secret = runCatching { Base32.decode(secretStr) }.getOrElse { return null }

        val algorithm =
            when (params["algorithm"]?.uppercase()) {
                "SHA256" -> OtpAlgorithm.SHA256
                "SHA512" -> OtpAlgorithm.SHA512
                else -> OtpAlgorithm.SHA1
            }
        val digits = params["digits"]?.toIntOrNull() ?: 6
        val period = params["period"]?.toIntOrNull() ?: 30
        val counter = params["counter"]?.toLongOrNull() ?: 0L

        // issuer: prefer query param; fall back to colon-prefix in label
        val colonIdx = label.indexOf(':')
        val issuerFromLabel = if (colonIdx > 0) label.substring(0, colonIdx) else null
        val issuer = params["issuer"] ?: issuerFromLabel

        return OtpUriParams(
            type = type,
            label = label,
            secret = secret,
            issuer = issuer,
            algorithm = algorithm,
            digits = digits,
            period = period,
            counter = counter,
        )
    }

    private fun decode(s: String): String = URLDecoder.decode(s, "UTF-8")
}
