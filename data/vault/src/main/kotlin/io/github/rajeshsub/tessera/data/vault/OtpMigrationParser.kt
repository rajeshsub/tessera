package io.github.rajeshsub.tessera.data.vault

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType
import java.net.URLDecoder
import java.util.Base64
import java.util.UUID

/**
 * Parses `otpauth-migration://` QR codes exported by Google Authenticator.
 * Uses a hand-rolled proto3 subset (varints + length-delimited) to avoid a
 * full protobuf-lite dependency.
 */
object OtpMigrationParser {
    fun parse(url: String): List<Account> {
        if (!url.startsWith("otpauth-migration://")) return emptyList()
        val dataParam = url.substringAfter("data=", "")
        if (dataParam.isEmpty()) return emptyList()
        return runCatching {
            val decoded = URLDecoder.decode(dataParam, "UTF-8")
            val protoBytes = Base64.getMimeDecoder().decode(decoded)
            parsePayload(protoBytes)
        }.getOrDefault(emptyList())
    }

    private fun parsePayload(bytes: ByteArray): List<Account> {
        val accounts = mutableListOf<Account>()
        val reader = ProtoReader(bytes)
        while (reader.hasMore()) {
            val tag = reader.readVarint().toInt()
            val fieldNumber = tag ushr 3
            val wireType = tag and 0x7
            if (fieldNumber == 1 && wireType == 2) {
                parseOtpParameters(reader.readLengthDelimited())?.let { accounts.add(it) }
            } else {
                reader.skipField(wireType)
            }
        }
        return accounts
    }

    @Suppress("CyclomaticComplexMethod")
    private fun parseOtpParameters(bytes: ByteArray): Account? {
        var secret: ByteArray? = null
        var name = ""
        var issuer: String? = null
        var algorithmInt = 1
        var typeInt = 2
        var digitsInt = 1
        var counter = 0L

        val reader = ProtoReader(bytes)
        while (reader.hasMore()) {
            val tag = reader.readVarint().toInt()
            val fieldNumber = tag ushr 3
            val wireType = tag and 0x7
            when {
                fieldNumber == 1 && wireType == 2 -> secret = reader.readLengthDelimited()
                fieldNumber == 2 && wireType == 2 -> name = reader.readString()
                fieldNumber == 3 && wireType == 2 -> issuer = reader.readString()
                fieldNumber == 4 && wireType == 0 -> algorithmInt = reader.readVarint().toInt()
                fieldNumber == 5 && wireType == 0 -> typeInt = reader.readVarint().toInt()
                fieldNumber == 6 && wireType == 0 -> digitsInt = reader.readVarint().toInt()
                fieldNumber == 7 && wireType == 0 -> counter = reader.readVarint()
                else -> reader.skipField(wireType)
            }
        }

        val s = secret ?: return null
        return Account(
            id = UUID.randomUUID().toString(),
            type = if (typeInt == 1) OtpType.HOTP else OtpType.TOTP,
            label = name,
            issuer = issuer?.takeIf { it.isNotEmpty() },
            secret = s,
            algorithm =
                when (algorithmInt) {
                    2 -> OtpAlgorithm.SHA256
                    3 -> OtpAlgorithm.SHA512
                    else -> OtpAlgorithm.SHA1
                },
            digits = if (digitsInt == 2) 8 else 6,
            period = 30,
            counter = counter,
        )
    }
}

@Suppress("MagicNumber")
private class ProtoReader(
    private val bytes: ByteArray,
) {
    private var pos = 0

    fun hasMore(): Boolean = pos < bytes.size

    fun readVarint(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = bytes[pos++].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) break
            shift += 7
        }
        return result
    }

    fun readLengthDelimited(): ByteArray {
        val len = readVarint().toInt()
        return bytes.copyOfRange(pos, pos + len).also { pos += len }
    }

    fun readString(): String = String(readLengthDelimited(), Charsets.UTF_8)

    fun skipField(wireType: Int) {
        when (wireType) {
            0 -> readVarint()
            1 -> pos += 8
            2 -> pos += readVarint().toInt()
            5 -> pos += 4
            else -> Unit
        }
    }
}
