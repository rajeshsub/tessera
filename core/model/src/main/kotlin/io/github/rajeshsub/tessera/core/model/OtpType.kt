package io.github.rajeshsub.tessera.core.model

/** OTP scheme. TOTP is time-based (RFC 6238); HOTP is counter-based (RFC 4226). */
enum class OtpType {
    TOTP,
    HOTP,
}

/** HMAC hash backing the OTP. */
enum class OtpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}
